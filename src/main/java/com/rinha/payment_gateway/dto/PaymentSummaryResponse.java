package com.rinha.payment_gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentSummaryResponse(
        @JsonProperty("default")
        PaymentSummary defaults,

        PaymentSummary fallback
){ }
