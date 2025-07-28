package com.rinha.payment_gateway.util;

import com.rinha.payment_gateway.model.PaymentProcessorStatus;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServiceHealthManager {

    private final ConcurrentHashMap<String, PaymentProcessorStatus> healthMap = new ConcurrentHashMap<>();

    public void setStatus(String serviceName, PaymentProcessorStatus status) {
        healthMap.put(serviceName, status);
    }

    public PaymentProcessorStatus getStatus(String serviceName) {
        return healthMap.getOrDefault(serviceName, PaymentProcessorStatus.UP);
    }

    public boolean isUp(String serviceName) {
        return getStatus(serviceName) == PaymentProcessorStatus.UP;
    }
}
