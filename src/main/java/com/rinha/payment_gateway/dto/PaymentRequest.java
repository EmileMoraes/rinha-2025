package com.rinha.payment_gateway.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        String correlationId,
        BigDecimal amount
) { }