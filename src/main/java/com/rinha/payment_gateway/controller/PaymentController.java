package com.rinha.payment_gateway.controller;

import com.rinha.payment_gateway.dto.PaymentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    @GetMapping("/health")
    public String health() {
        return "Status: UP";
    }

    @PostMapping
    public ResponseEntity<String> createPayment(@RequestBody PaymentRequest paymentRequest) {
        return ResponseEntity.ok().build();
    }
}
