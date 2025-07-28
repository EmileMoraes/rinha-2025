package com.rinha.payment_gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinha.payment_gateway.dto.PaymentRequest;
import com.rinha.payment_gateway.dto.PaymentSummary;
import com.rinha.payment_gateway.dto.PaymentSummaryResponse;
import com.rinha.payment_gateway.repository.Payment;
import com.rinha.payment_gateway.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final String PAYMENT_QUEUE = "payment_queue";

    public PaymentService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
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

    public void purgeData() {
        paymentRepository.deleteAll();
    }

    public PaymentSummaryResponse paymentSummary(Instant from,  Instant to) {
        List<Payment> paymentList;

        if (from != null && to != null) {
            paymentList = paymentRepository.findByProcessedAtBetween(from, to);
        } else if (from != null) {
            paymentList = paymentRepository.findByProcessedAtAfter(from);
        } else if (to != null) {
            paymentList = paymentRepository.findByProcessedAtBefore(to);
        } else {
            paymentList = paymentRepository.findAll();
        }

        Map<String, List<Payment>> listMap = paymentList.stream()
                .collect(Collectors.groupingBy(Payment::getProcessedBy));

        return new PaymentSummaryResponse(
                summarize(listMap.getOrDefault("default", List.of())),
                summarize(listMap.getOrDefault("fallback", List.of()))
        );
    }

    private PaymentSummary summarize(List<Payment> payments) {
        long total = payments.size();
        BigDecimal totalAmount = payments
                .stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PaymentSummary(total, totalAmount);
    }
}
