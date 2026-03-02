package com.compassed.compassed_api.api.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.security.CurrentUserService;
import com.compassed.compassed_api.service.PaymentService;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    /**
     * Create payment
     * POST /api/payments/create
     * Body: { "userId": 1, "subjectId": 1, "packageType": "PLACEMENT_PACK" }
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = currentUserService.requireCurrentUserId();
            Long subjectId = Long.parseLong(request.get("subjectId").toString());
            String packageType = request.get("packageType").toString();

            Map<String, Object> result = paymentService.createPayment(userId, subjectId, packageType);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error creating payment", e);
            return ResponseEntity.badRequest().body(Map.of("error", safeError(e)));
        }
    }

    /**
     * Get payment status
     * GET /api/payments/{paymentId}/status
     */
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<?> getPaymentStatus(
            @PathVariable Long paymentId) {
        try {
            Long userId = currentUserService.requireCurrentUserId();
            Map<String, Object> result = paymentService.getPaymentStatusForUser(userId, paymentId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error getting payment status", e);
            return ResponseEntity.badRequest().body(Map.of("error", safeError(e)));
        }
    }

    @GetMapping("/by-reference/{paymentReference}/status")
    public ResponseEntity<?> getPaymentStatusByReference(@PathVariable String paymentReference) {
        try {
            Long userId = currentUserService.requireCurrentUserId();
            Map<String, Object> result = paymentService.getPaymentStatusForUserByReference(userId, paymentReference);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting payment status by reference", e);
            return ResponseEntity.badRequest().body(Map.of("error", safeError(e)));
        }
    }

    @GetMapping("/latest-active/status")
    public ResponseEntity<?> getLatestActivePaymentStatus() {
        try {
            Long userId = currentUserService.requireCurrentUserId();
            Map<String, Object> result = paymentService.getLatestActivePaymentStatusForUser(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting latest active payment status", e);
            return ResponseEntity.badRequest().body(Map.of("error", safeError(e)));
        }
    }

    @PostMapping("/checkout-qr")
    public ResponseEntity<?> createCheckoutQr(@RequestBody Map<String, Object> request) {
        try {
            Long userId = currentUserService.requireCurrentUserId();
            List<Long> subjectIds = extractSubjectIds(request);
            Map<String, Object> result = paymentService.createCheckoutQrPayment(userId, subjectIds);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error creating checkout qr payment", e);
            return ResponseEntity.badRequest().body(Map.of("error", safeError(e)));
        }
    }

    @PostMapping("/{paymentId}/submit-transfer")
    public ResponseEntity<?> submitTransfer(
            @PathVariable Long paymentId,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            Long userId = currentUserService.requireCurrentUserId();
            String transferNote = request == null ? null : Objects.toString(request.get("transferNote"), null);
            Map<String, Object> result = paymentService.submitTransfer(userId, paymentId, transferNote);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error submitting transfer", e);
            return ResponseEntity.badRequest().body(Map.of("error", safeError(e)));
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
            return ResponseEntity.badRequest().body(Map.of("error", safeError(e)));
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

    @PostMapping("/callback/payos")
    @PermitAll
    public ResponseEntity<?> payosCallback(@RequestBody(required = false) Map<String, Object> payload) {
        try {
            log.info("Received PayOS callback");
            Map<String, Object> result = paymentService.handlePayOsWebhook(payload);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error processing PayOS callback", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private List<Long> extractSubjectIds(Map<String, Object> request) {
        if (request == null || request.get("subjectIds") == null) {
            return Collections.emptyList();
        }
        Object raw = request.get("subjectIds");
        if (!(raw instanceof List<?> list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(Long::parseLong)
                .toList();
    }

    private String safeError(Exception e) {
        String msg = e == null ? null : e.getMessage();
        if (e == null) {
            return "Unknown error";
        }
        String location = "";
        if (e.getStackTrace() != null && e.getStackTrace().length > 0) {
            StackTraceElement top = e.getStackTrace()[0];
            location = " @ " + top.getClassName() + ":" + top.getLineNumber();
        }
        if (msg == null || msg.isBlank()) {
            return e.getClass().getSimpleName() + location;
        }
        return msg + location;
    }
}
