package com.rinha.payment_gateway.service;

import com.rinha.payment_gateway.dto.HealthCheckResponse;
import com.rinha.payment_gateway.model.PaymentProcessorStatus;
import com.rinha.payment_gateway.util.ServiceHealthManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ServiceStatusScheduler {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ServiceHealthManager  serviceHealthManager;
    private final PaymentProcessingService  paymentProcessingService;
    private final String defaultBaseUrl;
    private final String fallbackBaseUrl;

    public ServiceStatusScheduler(ServiceHealthManager serviceHealthManager,  PaymentProcessingService  paymentProcessingService, @Value("${payment.processor.default-url}") String defaultBaseUrl,
                                  @Value("${payment.processor.fallback-url}") String fallbackBaseUrl) {
        this.serviceHealthManager = serviceHealthManager;
        this.paymentProcessingService = paymentProcessingService;
        this.defaultBaseUrl = defaultBaseUrl;
        this.fallbackBaseUrl = fallbackBaseUrl;
    }

    @Scheduled(fixedRate = 5000)
    public void checkServiceHealth() {
        String defaultHealthUrl = defaultBaseUrl + "/payments/service-health";
        String fallbackHealthUrl = fallbackBaseUrl + "/payments/service-health";
        updateServiceStatus("default", defaultHealthUrl);
        updateServiceStatus("fallBack", fallbackHealthUrl);

        System.out.println("Initiate payment service to get queue");
        paymentProcessingService.processQueue();
    }

    private void updateServiceStatus(String serviceName, String healthUrl) {
        try {
            HealthCheckResponse response = restTemplate.getForObject(healthUrl, HealthCheckResponse.class);
            System.out.println("Received response from service: " + serviceName + " response: " + response);
            boolean isHealthy = response != null && !response.isFailing();
            System.out.println("Service " + serviceName + " is healthy " + isHealthy);
            serviceHealthManager.setStatus(serviceName, isHealthy ? PaymentProcessorStatus.UP : PaymentProcessorStatus.DOWN);
        } catch (Exception e) {
            serviceHealthManager.setStatus(serviceName, PaymentProcessorStatus.DOWN);
        }
    }

}
