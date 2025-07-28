package com.rinha.payment_gateway.service;

import com.rinha.payment_gateway.dto.PaymentRequest;
import com.rinha.payment_gateway.model.PaymentProcessorStatus;
import com.rinha.payment_gateway.repository.Payment;
import com.rinha.payment_gateway.repository.PaymentRepository;
import com.rinha.payment_gateway.util.ServiceHealthManager;
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
        System.out.println("Scheduler is running...");

        PaymentProcessorStatus defaultStatus = serviceHealthManager.getStatus("default");
        PaymentProcessorStatus fallbackStatus = serviceHealthManager.getStatus("fallback");

        if (defaultStatus == PaymentProcessorStatus.DOWN && fallbackStatus == PaymentProcessorStatus.DOWN) {
            System.out.println("Services is DOWN");
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
                System.err.println("Error processing payment in both services " + payment.correlationId());
                break;
            }
        }
    }

    private boolean attemptToProcess(PaymentRequest paymentRequest, String processUrl) {
        try {
            restTemplate.postForEntity(processUrl, paymentRequest, String.class);
            System.out.println("Send message successfully " + paymentRequest);
            return true;
        }  catch (Exception e) {
            System.out.println("Error processing request to external API: " + e.getMessage());
            return false;
        }
    }

    private void saveSuccessfulPayment(PaymentRequest paymentRequest, String processedBy) {
        System.out.println("Saving payment to database: " + paymentRequest.correlationId());

        Payment payment = new Payment(
        paymentRequest.correlationId(),
        paymentRequest.amount(),
        processedBy,
                Instant.now()
        );
        paymentRepository.save(payment);
    }
}
