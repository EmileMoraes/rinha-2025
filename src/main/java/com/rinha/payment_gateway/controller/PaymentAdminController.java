package com.rinha.payment_gateway.controller;

import com.rinha.payment_gateway.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class PaymentAdminController {

    @Autowired
    private PaymentService  paymentService;

    @PostMapping("/purge-payments")
    public ResponseEntity<String> purgePayments() {
        paymentService.purgeData();
        return ResponseEntity.ok("All payments purged.");
    }
}
