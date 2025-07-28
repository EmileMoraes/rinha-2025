package com.rinha.payment_gateway;

import com.rinha.payment_gateway.model.HealthCheckResponse;
import com.rinha.payment_gateway.client.ServiceStatusScheduler;
import com.rinha.payment_gateway.util.ServiceHealthManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
public class ServiceStatusSchedulerIntegrationTest {
    @Autowired
    private ServiceStatusScheduler scheduler;

    @Autowired
    private ServiceHealthManager healthManager;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testHealthCheckRealIntegration() throws InterruptedException {
        Thread.sleep(5000);

        assertNotNull(healthManager.getStatus("default"));
        assertNotNull(healthManager.getStatus("fallback"));

        System.out.println("Default status: " + healthManager.getStatus("default"));
        System.out.println("Fallback status: " + healthManager.getStatus("fallback"));
    }

    @Test
    public void testHealthCheckRealUPIntegration() throws InterruptedException {
        ResponseEntity<HealthCheckResponse> response = restTemplate.getForEntity("http://localhost:8001/payments/service-health", HealthCheckResponse.class);
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isFailing());
    }
}
