package com.compassed.compassed_api.api.controller;

import com.compassed.compassed_api.api.dto.request.PaymentCallbackRequest;
import com.compassed.compassed_api.api.dto.request.PaymentCreateRequest;
import com.compassed.compassed_api.api.dto.response.PaymentCallbackResponse;
import com.compassed.compassed_api.api.dto.response.PaymentCreateResponse;
import com.compassed.compassed_api.service.IPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final IPaymentService paymentService;
    
    @PostMapping("/create")
    public ResponseEntity<PaymentCreateResponse> createPayment(
            @Valid @RequestBody PaymentCreateRequest request) {
        try {
            PaymentCreateResponse response = paymentService.createPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating payment", e);
            throw e;
        }
    }
    
    @PostMapping("/callback")
    public ResponseEntity<PaymentCallbackResponse> handleCallback(
            @Valid @RequestBody PaymentCallbackRequest request) {
        try {
            PaymentCallbackResponse response = paymentService.handleCallback(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error handling payment callback", e);
            throw e;
        }
    }
}
