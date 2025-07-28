package com.rinha.payment_gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinha.payment_gateway.dto.PaymentRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentQueue {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PAYMENT_QUEUE = "payment_queue";

    public PaymentQueue(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendPayment(PaymentRequest payment) {
        try {
            String json = objectMapper.writeValueAsString(payment);
            stringRedisTemplate.opsForList().rightPush(PAYMENT_QUEUE, json);
        }  catch (Exception e) {
            throw new RuntimeException("Error to send payment to Redis", e);
        }
    }
}
