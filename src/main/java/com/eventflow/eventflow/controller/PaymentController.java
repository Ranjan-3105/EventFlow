package com.eventflow.eventflow.controller;

import com.eventflow.eventflow.dto.request.CreatePaymentRequest;
import com.eventflow.eventflow.dto.request.VerifyPaymentRequest;
import com.eventflow.eventflow.dto.response.PaymentResponse;
import com.eventflow.eventflow.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService
    ) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request
    ) {

        PaymentResponse response =
                paymentService.createPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request
    ) {

        PaymentResponse response =
                paymentService.verifyPayment(request);

        return ResponseEntity.ok(response);
    }
}