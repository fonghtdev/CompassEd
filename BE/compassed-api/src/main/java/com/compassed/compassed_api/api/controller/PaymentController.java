package com.compassed.compassed_api.api.controller;

import com.compassed.compassed_api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * Create payment
     * POST /api/payments/create
     * Body: { "userId": 1, "subjectId": 1, "packageType": "PLACEMENT_PACK" }
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Long subjectId = Long.parseLong(request.get("subjectId").toString());
            String packageType = request.get("packageType").toString();
            
            Map<String, Object> result = paymentService.createPayment(userId, subjectId, packageType);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error creating payment", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get payment status
     * GET /api/payments/{paymentId}/status
     */
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long paymentId) {
        try {
            Map<String, Object> result = paymentService.getPaymentStatus(paymentId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error getting payment status", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * VNPay callback
     * POST /api/payments/callback/vnpay
     */
    @PostMapping("/callback/vnpay")
    public ResponseEntity<?> vnpayCallback(@RequestParam Map<String, String> params) {
        try {
            log.info("Received VNPay callback: {}", params);
            Map<String, Object> result = paymentService.verifyPaymentCallback(params);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error processing VNPay callback", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * VNPay callback (GET method - VNPay returns via GET)
     * GET /api/payments/callback/vnpay
     */
    @GetMapping("/callback/vnpay")
    public ResponseEntity<?> vnpayCallbackGet(@RequestParam Map<String, String> params) {
        return vnpayCallback(params);
    }
}
