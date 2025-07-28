package com.rinha.payment_gateway.controller;

import com.rinha.payment_gateway.dto.PaymentRequest;
import com.rinha.payment_gateway.dto.PaymentSummaryResponse;
import com.rinha.payment_gateway.repository.PaymentRepository;
import com.rinha.payment_gateway.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
public class PaymentController {

   @Autowired
   private PaymentService paymentService;

    @Autowired
   private PaymentRepository paymentRepository;

    @GetMapping("/health")
    public String health() {
        return "Status: UP";
    }

    @PostMapping("/payments")
    public ResponseEntity<String> createPayment(@RequestBody PaymentRequest paymentRequest) {
        paymentService.sendPayment(paymentRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payments-summary")
    public ResponseEntity<PaymentSummaryResponse> getPaymentSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        PaymentSummaryResponse response = paymentService.paymentSummary(from, to);
        return ResponseEntity.ok(response);
    }
}
