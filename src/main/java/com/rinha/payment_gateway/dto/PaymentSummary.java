package com.rinha.payment_gateway.dto;

import java.math.BigDecimal;

public record PaymentSummary(
        long totalRequests,
        BigDecimal totalAmount
) { }
