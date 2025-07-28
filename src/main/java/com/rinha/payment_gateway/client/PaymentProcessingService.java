package com.rinha.payment_gateway.client;

import com.rinha.payment_gateway.dto.PaymentRequest;
import com.rinha.payment_gateway.model.PaymentProcessorStatus;
import com.rinha.payment_gateway.repository.Payment;
import com.rinha.payment_gateway.repository.PaymentRepository;
import com.rinha.payment_gateway.util.ServiceHealthManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
public class PaymentProcessingService {

    private final ServiceHealthManager serviceHealthManager;
    private final RestTemplate restTemplate = new RestTemplate();
    private final RedisTemplate<String, PaymentRequest> redisTemplate;
    private final PaymentRepository paymentRepository;

    Logger logger = LoggerFactory.getLogger(PaymentProcessingService.class);

    private final String defaultProcessUrl;
    private final String fallbackProcessUrl;

    private final String paymentQueueKey;

    public PaymentProcessingService(ServiceHealthManager serviceHealthManager, RedisTemplate<String, PaymentRequest> redisTemplate,  PaymentRepository paymentRepository, @Value("${payment.processor.default-url}") String defaultProcessorBaseUrl,
                                    @Value("${payment.processor.fallback-url}") String fallbackProcessorBaseUrl, @Value("${payment.queue.name}") String paymentQueueKey) {
        this.serviceHealthManager = serviceHealthManager;
        this.redisTemplate = redisTemplate;
        this.paymentRepository = paymentRepository;
        this.defaultProcessUrl = defaultProcessorBaseUrl + "/payments";
        this.fallbackProcessUrl = fallbackProcessorBaseUrl + "/payments";
        this.paymentQueueKey = paymentQueueKey;
    }

    public void processQueue(){
        logger.info("Scheduler is running...");

        PaymentProcessorStatus defaultStatus = serviceHealthManager.getStatus("default");
        PaymentProcessorStatus fallbackStatus = serviceHealthManager.getStatus("fallback");

        if (defaultStatus == PaymentProcessorStatus.DOWN && fallbackStatus == PaymentProcessorStatus.DOWN) {
            logger.info("Default and Fallback are DOWN");
            return;
        }

        while (true) {
            PaymentRequest payment = redisTemplate.opsForList().leftPop(paymentQueueKey);
            if (payment == null) {
                break;
            }

            boolean processed = false;
            String processedBy = null;

            if(defaultStatus == PaymentProcessorStatus.UP){
                if(attemptToProcess(payment, defaultProcessUrl)) {
                    processed = true;
                    processedBy = "default";
                }
            }
            else if(!processed && fallbackStatus == PaymentProcessorStatus.UP){
                if(attemptToProcess(payment, fallbackProcessUrl)) {
                    processed = true;
                    processedBy = "fallback";
                }
            }


            if(processed){
                saveSuccessfulPayment(payment, processedBy);
            } else {
                redisTemplate.opsForList().rightPush(paymentQueueKey, payment);
                logger.error("Error processing payment in both services {}", payment.correlationId());
                break;
            }
        }
    }

    private boolean attemptToProcess(PaymentRequest paymentRequest, String processUrl) {
        try {
            restTemplate.postForEntity(processUrl, paymentRequest, String.class);
            logger.info("Successfully sending payment request {}", paymentRequest);
            return true;
        }  catch (Exception e) {
            logger.error("Error processing request to external API: {}", e.getMessage());
            return false;
        }
    }

    private void saveSuccessfulPayment(PaymentRequest paymentRequest, String processedBy) {
        logger.info("Saving payment to database: {}", paymentRequest.correlationId());

        Payment payment = new Payment(
        paymentRequest.correlationId(),
        paymentRequest.amount(),
        processedBy,
                Instant.now()
        );
        paymentRepository.save(payment);
    }
}
