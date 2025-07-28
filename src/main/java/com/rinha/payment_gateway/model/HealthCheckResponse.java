package com.rinha.payment_gateway.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthCheckResponse {
    private boolean failing;
    private int minResponseTime;
}
