package com.rinha.payment_gateway.dto;

public record PaymentRequest(
        String correlationId,
        Double amount
) { }