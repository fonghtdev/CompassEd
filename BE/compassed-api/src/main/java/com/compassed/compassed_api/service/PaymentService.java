package com.compassed.compassed_api.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.compassed.compassed_api.domain.entity.Payment;
import com.compassed.compassed_api.domain.entity.PaymentSubjectItem;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.entity.Subscription;
import com.compassed.compassed_api.repository.PaymentRepository;
import com.compassed.compassed_api.repository.PaymentSubjectItemRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.SubscriptionRepository;
import com.compassed.compassed_api.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentSubjectItemRepository paymentSubjectItemRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final PricingService pricingService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${vnpay.tmn-code:DEMO}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret:DEMO_SECRET_KEY}")
    private String vnpHashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpUrl;

    @Value("${vnpay.return-url:http://localhost:3000/payment/callback}")
    private String vnpReturnUrl;

    @Value("${checkout.qr.image-url:https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=CompassED_Payment_Placeholder}")
    private String checkoutQrImageUrl;

    @Value("${checkout.qr.account-name:COMPASSED}")
    private String checkoutQrAccountName;

    @Value("${checkout.qr.account-no:0000000000}")
    private String checkoutQrAccountNo;

    @Value("${checkout.qr.bank:VietQR}")
    private String checkoutQrBank;

    @Value("${checkout.qr.bank-bin:970422}")
    private String checkoutQrBankBin;

    @Value("${payos.enabled:false}")
    private boolean payOsEnabled;

    @Value("${payos.client-id:}")
    private String payOsClientId;

    @Value("${payos.api-key:}")
    private String payOsApiKey;

    @Value("${payos.checksum-key:}")
    private String payOsChecksumKey;

    @Value("${payos.base-url:https://api-merchant.payos.vn}")
    private String payOsBaseUrl;

    @Value("${payos.return-url:https://compassed.io.vn/checkout}")
    private String payOsReturnUrl;

    @Value("${payos.cancel-url:https://compassed.io.vn/checkout}")
    private String payOsCancelUrl;

    @Value("${payos.check-cooldown-seconds:15}")
    private long payOsCheckCooldownSeconds;

    @PostConstruct
    void logPayOsConfigSummary() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);
        restTemplate.setRequestFactory(requestFactory);
        log.info(
                "PayOS config: enabled={}, clientIdSet={}, apiKeyLen={}, checksumKeyLen={}, baseUrl={}",
                payOsEnabled,
                !isBlank(payOsClientId),
                payOsApiKey == null ? 0 : payOsApiKey.length(),
                payOsChecksumKey == null ? 0 : payOsChecksumKey.length(),
                payOsBaseUrl);
    }

    @Transactional
    public Map<String, Object> createPayment(Long userId, Long subjectId, String packageType) {
        BigDecimal amount = calculateAmount(packageType);

        Payment payment = Payment.builder()
                .userId(userId)
                .amount(amount)
                .currency("VND")
                .paymentMethod("VNPAY")
                .paymentGateway("VNPAY")
                .subjectId(subjectId)
                .packageType(packageType)
                .status("PENDING")
                .build();
        payment = paymentRepository.save(payment);

        String paymentUrl = generateVNPayUrl(payment);

        return Map.of(
                "paymentId", payment.getId(),
                "amount", amount,
                "currency", "VND",
                "paymentUrl", paymentUrl,
                "status", "PENDING");
    }

    @Transactional
    public Map<String, Object> verifyPaymentCallback(Map<String, String> params) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            String signValue = hashAllFields(params);

            if (!signValue.equals(vnpSecureHash)) {
                log.error("Invalid signature");
                return Map.of("success", false, "message", "Invalid signature");
            }

            String transactionId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");

            Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
            if (paymentOpt.isEmpty()) {
                String paymentIdStr = transactionId.replace("PAY", "");
                Long paymentId = Long.parseLong(paymentIdStr);
                paymentOpt = paymentRepository.findById(paymentId);
            }

            if (paymentOpt.isEmpty()) {
                return Map.of("success", false, "message", "Payment not found");
            }

            Payment payment = paymentOpt.get();

            if ("00".equals(responseCode)) {
                payment.setStatus("SUCCESS");
                payment.setTransactionId(params.get("vnp_TransactionNo"));
                payment.setConfirmedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                createSubscriptionAfterPayment(payment.getUserId(), payment.getSubjectId());

                return Map.of(
                        "success", true,
                        "message", "Payment successful",
                        "paymentId", payment.getId(),
                        "amount", payment.getAmount());
            }

            payment.setStatus("FAILED");
            paymentRepository.save(payment);
            return Map.of("success", false, "message", "Payment failed: " + responseCode);

        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    public Map<String, Object> getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return paymentStatusPayload(payment);
    }

    public Map<String, Object> getPaymentStatusForUser(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        // Force refresh with PayOS on status polling so FE can unlock near real-time
        // after paid.
        payment = refreshPayOsStatus(payment, true);
        return paymentStatusPayload(payment);
    }

    public Map<String, Object> getPaymentStatusForUserByReference(Long userId, String paymentReference) {
        String ref = paymentReference == null ? "" : paymentReference.trim();
        if (ref.isBlank()) {
            throw new RuntimeException("paymentReference is required");
        }
        Payment payment = paymentRepository.findByPaymentReferenceAndUserId(ref, userId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment = refreshPayOsStatus(payment, true);
        return paymentStatusPayload(payment);
    }

    public Map<String, Object> getLatestActivePaymentStatusForUser(Long userId) {
        List<Payment> active = paymentRepository.findByUserIdAndStatusInOrderByIdDesc(
                userId,
                List.of("PENDING", "SUBMITTED"));
        if (active == null || active.isEmpty()) {
            return Map.of("paymentId", null, "status", "NONE", "subjectIds", List.of());
        }
        Payment payment = refreshPayOsStatus(active.get(0), true);
        return paymentStatusPayload(payment);
    }

    @Transactional
    public Map<String, Object> handlePayOsWebhook(Map<String, Object> payload) {
        if (payload == null) {
            return Map.of("ok", true, "message", "ignored");
        }
        Map<String, Object> top = toStringKeyMap(payload);
        Object dataObj = top.get("data");
        Map<String, Object> data = dataObj instanceof Map<?, ?> rawData ? toStringKeyMap(rawData) : Map.of();

        String orderCode = Objects.toString(data.get("orderCode"), "").trim();
        if (orderCode.isEmpty()) {
            orderCode = Objects.toString(top.get("orderCode"), "").trim();
        }
        if (orderCode.isEmpty()) {
            return Map.of("ok", true, "message", "ignored");
        }

        Payment payment = paymentRepository.findByPaymentReference(orderCode)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderCode=" + orderCode));

        String webhookStatus = resolveWebhookPayOsStatus(top, data);
        if ("PAID".equals(webhookStatus) || "SUCCESS".equals(webhookStatus)) {
            List<Long> subjectIds = resolveSubjectIds(payment);
            for (Long subjectId : subjectIds) {
                createSubscriptionAfterPayment(payment.getUserId(), subjectId);
            }
            payment.setStatus("SUCCESS");
            payment.setConfirmedAt(LocalDateTime.now());
            String paymentLinkId = Objects.toString(data.get("paymentLinkId"), "").trim();
            if (paymentLinkId.isEmpty()) {
                paymentLinkId = Objects.toString(data.get("id"), "").trim();
            }
            if (!paymentLinkId.isEmpty()) {
                payment.setTransactionId(paymentLinkId);
            }
            payment = paymentRepository.save(payment);
        } else if ("CANCELLED".equals(webhookStatus) || "FAILED".equals(webhookStatus) || "EXPIRED".equals(webhookStatus)) {
            if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                payment.setStatus("FAILED");
                String reason = extractPayOsCancellationReason(data);
                if (reason != null && !reason.isBlank()) {
                    payment.setTransferNote(sanitizeTransferNote("PAYOS_" + webhookStatus + ": " + reason));
                }
                payment = paymentRepository.save(payment);
            }
        }

        Payment synced = refreshPayOsStatus(payment, true);
        return Map.of(
                "ok", true,
                "paymentId", synced.getId(),
                "status", synced.getStatus(),
                "orderCode", orderCode);
    }

    public List<Map<String, Object>> getPaymentsForAdmin(String status) {
        List<Payment> payments;
        if (status == null || status.isBlank()) {
            payments = paymentRepository.findAllByOrderByCreatedAtDesc();
        } else {
            payments = paymentRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        }
        return payments.stream().map(this::paymentStatusPayload).toList();
    }

    @Transactional
    public Map<String, Object> createCheckoutQrPayment(Long userId, List<Long> requestSubjectIds) {
        List<Long> subjectIds = Optional.ofNullable(requestSubjectIds)
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (subjectIds.isEmpty()) {
            throw new RuntimeException("Must select at least 1 subject");
        }
        if (!payOsEnabled || isBlank(payOsClientId) || isBlank(payOsApiKey) || isBlank(payOsChecksumKey)) {
            throw new RuntimeException("PayOS is not configured");
        }

        List<Subject> subjects = subjectRepository.findAllById(subjectIds);
        if (subjects.size() != subjectIds.size()) {
            throw new RuntimeException("Some subjects not found");
        }

        long totalAmountVnd = pricingService.calculateTotalAmountVnd(subjectIds.size());

        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAmount(BigDecimal.valueOf(totalAmountVnd));
        payment.setCurrency("VND");
        payment.setPaymentMethod("PAYOS");
        payment.setPaymentGateway("PAYOS");
        payment.setPackageType("SUBJECT_BUNDLE_" + subjectIds.size());
        payment.setStatus("PENDING");
        Payment persistedPayment = paymentRepository.save(payment);

        long orderCode = buildOrderCode(persistedPayment.getId());
        String orderCodeStr = String.valueOf(orderCode);
        String description = buildPaymentDescription(persistedPayment.getId());
        Map<String, Object> payOsData = createPayOsPaymentLink(orderCode, totalAmountVnd, description, subjects);

        persistedPayment.setPaymentReference(orderCodeStr);
        persistedPayment.setTransferNote(description);
        persistedPayment.setTransactionId(Objects.toString(payOsData.get("paymentLinkId"), null));
        persistedPayment = paymentRepository.save(persistedPayment);

        Payment paymentForItems = persistedPayment;
        List<PaymentSubjectItem> items = subjectIds.stream().map(subjectId -> {
            PaymentSubjectItem item = new PaymentSubjectItem();
            item.setPayment(paymentForItems);
            item.setSubjectId(subjectId);
            return item;
        }).toList();
        paymentSubjectItemRepository.saveAll(items);

        String qrCodeText = Objects.toString(payOsData.get("qrCode"), "");
        String bin = Objects.toString(payOsData.get("bin"), checkoutQrBankBin);
        String accountNumber = Objects.toString(payOsData.get("accountNumber"), checkoutQrAccountNo);
        String accountName = Objects.toString(payOsData.get("accountName"), checkoutQrAccountName);

        Map<String, Object> payload = paymentStatusPayload(persistedPayment);
        payload.put("subjectIds", subjectIds);
        payload.put("subjectCount", subjectIds.size());
        payload.put("qrImageUrl", buildCheckoutQrImageUrlFromText(qrCodeText));
        payload.put("qrCode", qrCodeText);
        payload.put("checkoutUrl", payOsData.get("checkoutUrl"));
        payload.put("bankName", checkoutQrBank);
        payload.put("bankBin", bin);
        payload.put("accountNo", accountNumber);
        payload.put("accountName", accountName);
        payload.put("transferContent", description);
        return payload;
    }

    @Transactional
    public Map<String, Object> submitTransfer(Long userId, Long paymentId, String transferNote) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            return paymentStatusPayload(payment);
        }

        payment.setStatus("SUBMITTED");
        payment.setTransferNote(sanitizeTransferNote(transferNote));
        payment.setSubmittedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        return paymentStatusPayload(payment);
    }

    @Transactional
    public Map<String, Object> approveManualPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            return paymentStatusPayload(payment);
        }
        if (!"SUBMITTED".equalsIgnoreCase(payment.getStatus()) && !"PENDING".equalsIgnoreCase(payment.getStatus())) {
            throw new RuntimeException("Cannot approve payment with status=" + payment.getStatus());
        }

        List<Long> subjectIds = resolveSubjectIds(payment);
        if (subjectIds.isEmpty()) {
            throw new RuntimeException("Payment has no subjects");
        }

        for (Long subjectId : subjectIds) {
            createSubscriptionAfterPayment(payment.getUserId(), subjectId);
        }

        payment.setStatus("SUCCESS");
        payment.setConfirmedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        return paymentStatusPayload(payment);
    }

    @Transactional
    public Map<String, Object> rejectManualPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            throw new RuntimeException("Cannot reject successful payment");
        }
        payment.setStatus("FAILED");
        String trimmedReason = reason == null ? "" : reason.trim();
        if (!trimmedReason.isEmpty()) {
            payment.setTransferNote(sanitizeTransferNote("REJECTED: " + trimmedReason));
        }
        payment = paymentRepository.save(payment);
        return paymentStatusPayload(payment);
    }

    private Payment maybeAutoConfirmWithPayOs(Payment payment) {
        return refreshPayOsStatus(payment, false);
    }

    private Payment refreshPayOsStatus(Payment payment, boolean forceCheck) {
        String status = String.valueOf(payment.getStatus()).toUpperCase();
        if (!"PENDING".equals(status) && !"SUBMITTED".equals(status)) {
            return payment;
        }
        if (!payOsEnabled || isBlank(payOsClientId) || isBlank(payOsApiKey)) {
            return payment;
        }
        if (payment.getPaymentReference() == null || payment.getPaymentReference().isBlank()) {
            return payment;
        }
        LocalDateTime lastCheckedAt = payment.getLastCheckedAt();
        if (!forceCheck && lastCheckedAt != null
                && lastCheckedAt.isAfter(LocalDateTime.now().minusSeconds(payOsCheckCooldownSeconds))) {
            return payment;
        }

        payment.setLastCheckedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        try {
            Map<String, Object> payOsStatus = fetchPayOsPaymentStatusWithFallback(payment);
            if (payOsStatus.isEmpty()) {
                return payment;
            }

            String statusValue = extractPayOsStatus(payOsStatus);
            String cancellationReason = extractPayOsCancellationReason(payOsStatus);
            String paymentLinkId = Objects.toString(payOsStatus.get("id"), null);

            if (statusValue.isBlank()) {
                log.warn("PayOS status payload does not contain status for paymentId={}, orderCode={}, payload={}",
                        payment.getId(), payment.getPaymentReference(), payOsStatus);
                return payment;
            }

            if ("PAID".equals(statusValue) || "SUCCESS".equals(statusValue)) {
                List<Long> subjectIds = resolveSubjectIds(payment);
                for (Long subjectId : subjectIds) {
                    createSubscriptionAfterPayment(payment.getUserId(), subjectId);
                }
                payment.setStatus("SUCCESS");
                payment.setConfirmedAt(LocalDateTime.now());
                if (paymentLinkId != null && !paymentLinkId.isBlank()) {
                    payment.setTransactionId(paymentLinkId);
                }
                return paymentRepository.save(payment);
            }

            if ("CANCELLED".equals(statusValue) || "EXPIRED".equals(statusValue) || "FAILED".equals(statusValue)) {
                payment.setStatus("FAILED");
                if (cancellationReason != null && !cancellationReason.isBlank()) {
                    payment.setTransferNote(sanitizeTransferNote("PAYOS_" + statusValue + ": " + cancellationReason));
                }
                return paymentRepository.save(payment);
            }

            return payment;
        } catch (Exception ex) {
            log.warn("PayOS check failed for paymentId={}, orderCode={}: {}", payment.getId(), payment.getPaymentReference(), ex.getMessage());
            return payment;
        }
    }

    @Scheduled(fixedDelayString = "${payos.sync-fixed-delay-ms:8000}")
    @Transactional
    public void syncPendingPaymentsInBackground() {
        if (!payOsEnabled || isBlank(payOsClientId) || isBlank(payOsApiKey)) {
            return;
        }
        List<Payment> pending = new ArrayList<>();
        pending.addAll(paymentRepository.findByStatus("PENDING"));
        pending.addAll(paymentRepository.findByStatus("SUBMITTED"));
        if (pending.isEmpty()) {
            return;
        }
        int max = Math.min(100, pending.size());
        for (int i = 0; i < max; i++) {
            Payment p = pending.get(i);
            try {
                refreshPayOsStatus(p, true);
            } catch (Exception ex) {
                log.debug("Background sync failed for paymentId={}: {}", p.getId(), ex.getMessage());
            }
        }
    }

    private Map<String, Object> fetchPayOsPaymentStatusWithFallback(Payment payment) {
        Map<String, Object> byReference = fetchPayOsPaymentStatus(payment.getPaymentReference());
        String byReferenceStatus = extractPayOsStatus(byReference);
        if (isTerminalPayOsStatus(byReferenceStatus)) {
            return byReference;
        }

        String paymentLinkId = safeTrim(payment.getTransactionId());
        if (!paymentLinkId.isBlank()) {
            Map<String, Object> byLinkId = fetchPayOsPaymentStatus(paymentLinkId);
            String byLinkIdStatus = extractPayOsStatus(byLinkId);
            if (isTerminalPayOsStatus(byLinkIdStatus)) {
                return byLinkId;
            }
            if (byReferenceStatus.isBlank() && !byLinkIdStatus.isBlank()) {
                return byLinkId;
            }
        }
        return byReference;
    }

    private Map<String, Object> fetchPayOsPaymentStatus(String orderCode) {
        String normalizedBase = normalizeBaseUrl(payOsBaseUrl);
        String url = normalizedBase + "/v2/payment-requests/" + orderCode;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", safeTrim(payOsClientId));
        headers.set("x-api-key", safeTrim(payOsApiKey));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<?, ?> responseBody = response.getBody();
        if (responseBody == null) {
            return Map.of();
        }
        Object code = responseBody.get("code");
        Object data = responseBody.get("data");

        // Accept multiple code formats (00 / 0 / null), and fall back to top-level payload
        // if provider returns status fields directly.
        if (code != null && !isPayOsSuccessCode(code)) {
            String desc = Objects.toString(responseBody.get("desc"), "");
            log.warn("PayOS status API returned non-success code={}, desc={}, orderCode={}", code, desc, orderCode);
            if (!(data instanceof Map<?, ?>)) {
                return Map.of();
            }
        }
        if (data instanceof Map<?, ?> dataMap) {
            return toStringKeyMap(dataMap);
        }
        Map<String, Object> top = toStringKeyMap(responseBody);
        if (top.containsKey("status") || top.containsKey("paymentStatus") || top.containsKey("state")) {
            return top;
        }
        return Map.of();
    }

    private Map<String, Object> createPayOsPaymentLink(
            long orderCode,
            long amountVnd,
            String description,
            List<Subject> subjects) {
        String normalizedBase = normalizeBaseUrl(payOsBaseUrl);
        String url = normalizedBase + "/v2/payment-requests";

        List<Map<String, Object>> items = buildPayOsItems(amountVnd, subjects);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("orderCode", orderCode);
        requestBody.put("amount", amountVnd);
        requestBody.put("description", description);
        requestBody.put("returnUrl", payOsReturnUrl);
        requestBody.put("cancelUrl", payOsCancelUrl);
        requestBody.put("items", items);
        requestBody.put("signature", signPayOsCreateRequest(orderCode, amountVnd, description));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", safeTrim(payOsClientId));
        headers.set("x-api-key", safeTrim(payOsApiKey));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map<?, ?> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("PayOS create payment returned empty body");
            }
            Object code = responseBody.get("code");
            if (code != null && !"00".equals(String.valueOf(code))) {
                String desc = Objects.toString(responseBody.get("desc"), "PayOS create payment failed");
                throw new RuntimeException("PayOS error: " + desc);
            }
            Object data = responseBody.get("data");
            if (!(data instanceof Map<?, ?> dataMap)) {
                throw new RuntimeException("PayOS create payment missing data");
            }
            return toStringKeyMap(dataMap);
        } catch (HttpStatusCodeException ex) {
            throw new RuntimeException("PayOS create payment failed: " + ex.getResponseBodyAsString());
        }
    }

    private List<Map<String, Object>> buildPayOsItems(long amountVnd, List<Subject> subjects) {
        if (subjects.isEmpty()) {
            return List.of();
        }
        long basePrice = amountVnd / subjects.size();
        long remainder = amountVnd - (basePrice * subjects.size());
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < subjects.size(); i++) {
            Subject subject = subjects.get(i);
            long itemPrice = basePrice + (i == 0 ? remainder : 0);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", buildSafePayOsItemName(subject));
            item.put("quantity", 1);
            item.put("price", itemPrice);
            items.add(item);
        }
        return items;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api-merchant.payos.vn";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String generateVNPayUrl(Payment payment) {
        try {
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnpTmnCode);
            vnpParams.put("vnp_Amount", String.valueOf(payment.getAmount().multiply(new BigDecimal(100)).longValue()));
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", "PAY" + payment.getId());
            vnpParams.put("vnp_OrderInfo", "Payment for " + payment.getPackageType());
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
            vnpParams.put("vnp_IpAddr", "127.0.0.1");

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnpSecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

            return vnpUrl + "?" + queryUrl;

        } catch (Exception e) {
            log.error("Error generating VNPay URL", e);
            throw new RuntimeException("Error generating payment URL");
        }
    }

    private void createSubscriptionAfterPayment(Long userId, Long subjectId) {
        if (subjectId == null) {
            return;
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (subscriptionRepository.existsByUserIdAndSubjectIdAndIsActiveTrue(userId, subjectId)) {
            return;
        }
        Optional<Subscription> existingOpt = subscriptionRepository.findTopByUserIdAndSubjectIdOrderByIdDesc(userId, subjectId);

        Subscription subscription = existingOpt.orElseGet(Subscription::new);
        subscription.setUserId(userId);
        subscription.setSubjectId(subjectId);
        if (subscription.getPackageId() == null) {
            subscription.setPackageId(0L);
        }
        if (subscription.getPaymentId() == null) {
            subscription.setPaymentId(0L);
        }
        if (subscription.getStartDate() == null) {
            subscription.setStartDate(LocalDateTime.now());
        }
        if (subscription.getEndDate() == null) {
            subscription.setEndDate(LocalDateTime.now().plusYears(1));
        }
        subscription.setIsActive(true);
        if (subscription.getPlacementUnlocked() == null) {
            subscription.setPlacementUnlocked(false);
        }

        subscriptionRepository.save(subscription);
        log.info("Created subscription for userId={}, subjectId={}", userId, subjectId);
    }

    private BigDecimal calculateAmount(String packageType) {
        return switch (packageType) {
            case "PLACEMENT_PACK" -> new BigDecimal("299000");
            case "SUBSCRIPTION_MONTHLY" -> new BigDecimal("499000");
            case "SUBSCRIPTION_3MONTHS" -> new BigDecimal("1299000");
            case "SUBSCRIPTION_6MONTHS" -> new BigDecimal("2499000");
            default -> new BigDecimal("299000");
        };
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error computing HMAC", e);
            return "";
        }
    }

    private String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                sb.append(fieldName);
                sb.append('=');
                sb.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    sb.append('&');
                }
            }
        }
        return hmacSHA512(vnpHashSecret, sb.toString());
    }

    private List<Long> resolveSubjectIds(Payment payment) {
        List<Long> ids = paymentSubjectItemRepository.findByPayment_Id(payment.getId())
                .stream()
                .map(PaymentSubjectItem::getSubjectId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        if (ids.isEmpty() && payment.getSubjectId() != null) {
            ids.add(payment.getSubjectId());
        }
        return ids;
    }

    private Map<String, Object> paymentStatusPayload(Payment payment) {
        Map<String, Object> payload = new LinkedHashMap<>();
        Payment checkedPayment = maybeAutoConfirmWithPayOs(payment);
        payload.put("paymentId", checkedPayment.getId());
        payload.put("status", checkedPayment.getStatus());
        payload.put("amount", checkedPayment.getAmount());
        payload.put("currency", checkedPayment.getCurrency());
        payload.put("createdAt", checkedPayment.getCreatedAt());
        payload.put("submittedAt", checkedPayment.getSubmittedAt());
        payload.put("confirmedAt", checkedPayment.getConfirmedAt());
        payload.put("paymentGateway", checkedPayment.getPaymentGateway());
        payload.put("transferNote", checkedPayment.getTransferNote());
        payload.put("paymentReference", checkedPayment.getPaymentReference());
        payload.put("subjectIds", resolveSubjectIds(checkedPayment));
        return payload;
    }

    private String sanitizeTransferNote(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() <= 255 ? trimmed : trimmed.substring(0, 255);
    }

    private long buildOrderCode(Long paymentId) {
        long base = System.currentTimeMillis() % 1_000_000_000L;
        return base * 1000L + (paymentId % 1000L);
    }

    private String buildPaymentDescription(Long paymentId) {
        return "CE" + paymentId;
    }

    private String buildCheckoutQrImageUrlFromText(String qrCodeText) {
        if (qrCodeText == null || qrCodeText.isBlank()) {
            return checkoutQrImageUrl;
        }
        return "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" + encodeQueryValue(qrCodeText);
    }

    private String encodeQueryValue(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String signPayOsCreateRequest(long orderCode, long amountVnd, String description) {
        if (isBlank(payOsChecksumKey)) {
            throw new RuntimeException("PAYOS_CHECKSUM_KEY is missing");
        }
        String payload = "amount=" + amountVnd
                + "&cancelUrl=" + payOsCancelUrl
                + "&description=" + description
                + "&orderCode=" + orderCode
                + "&returnUrl=" + payOsReturnUrl;
        return hmacSHA256(payOsChecksumKey, payload);
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Cannot sign PayOS request", ex);
        }
    }

    private Map<String, Object> toStringKeyMap(Map<?, ?> source) {
        Map<String, Object> output = new LinkedHashMap<>();
        if (source == null) {
            return output;
        }
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            String key = String.valueOf(entry.getKey());
            output.put(key, entry.getValue());
        }
        return output;
    }

    private String trimForPayOs(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.length() <= maxLen) {
            return normalized;
        }
        return normalized.substring(0, maxLen);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isPayOsSuccessCode(Object code) {
        if (code == null) {
            return true;
        }
        String value = String.valueOf(code).trim();
        return "00".equals(value) || "0".equals(value);
    }

    private String extractPayOsStatus(Map<String, Object> payload) {
        String status = Objects.toString(payload.get("status"), "").trim();
        if (status.isBlank()) {
            status = Objects.toString(payload.get("paymentStatus"), "").trim();
        }
        if (status.isBlank()) {
            status = Objects.toString(payload.get("state"), "").trim();
        }
        if (status.isBlank()) {
            status = Objects.toString(payload.get("paymentLinkStatus"), "").trim();
        }
        if (!status.isBlank()) {
            return status.toUpperCase();
        }

        String paidAt = Objects.toString(payload.get("paidAt"), "").trim();
        if (!paidAt.isBlank()) {
            return "PAID";
        }

        Object paidObj = payload.get("paid");
        if (paidObj instanceof Boolean b && b) {
            return "PAID";
        }

        try {
            double amount = Double.parseDouble(String.valueOf(payload.get("amount")));
            double amountPaid = Double.parseDouble(String.valueOf(payload.get("amountPaid")));
            if (amount > 0 && amountPaid >= amount) {
                return "PAID";
            }
        } catch (Exception ignored) {
        }

        Object transactionsObj = payload.get("transactions");
        if (transactionsObj instanceof List<?> txs) {
            for (int i = txs.size() - 1; i >= 0; i--) {
                Object tx = txs.get(i);
                if (!(tx instanceof Map<?, ?> txMap)) {
                    continue;
                }
                Map<String, Object> txPayload = toStringKeyMap(txMap);
                String txStatus = Objects.toString(txPayload.get("status"), "").trim();
                if (txStatus.isBlank()) {
                    txStatus = Objects.toString(txPayload.get("paymentStatus"), "").trim();
                }
                if (!txStatus.isBlank()) {
                    txStatus = txStatus.toUpperCase();
                    if ("PAID".equals(txStatus) || "SUCCESS".equals(txStatus)) {
                        return "PAID";
                    }
                    if ("CANCELLED".equals(txStatus) || "FAILED".equals(txStatus) || "EXPIRED".equals(txStatus)) {
                        return txStatus;
                    }
                }
            }
        }
        return "";
    }

    private boolean isTerminalPayOsStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String upper = status.toUpperCase();
        return "PAID".equals(upper)
                || "SUCCESS".equals(upper)
                || "FAILED".equals(upper)
                || "EXPIRED".equals(upper)
                || "CANCELLED".equals(upper);
    }

    private String extractPayOsCancellationReason(Map<String, Object> payload) {
        String reason = Objects.toString(payload.get("cancellationReason"), "").trim();
        if (reason.isBlank()) {
            reason = Objects.toString(payload.get("cancelReason"), "").trim();
        }
        if (reason.isBlank()) {
            reason = Objects.toString(payload.get("reason"), "").trim();
        }
        if (reason.isBlank()) {
            reason = Objects.toString(payload.get("desc"), "").trim();
        }
        return reason;
    }

    private String resolveWebhookPayOsStatus(Map<String, Object> top, Map<String, Object> data) {
        String status = extractPayOsStatus(data);
        if (status.isBlank()) {
            status = extractPayOsStatus(top);
        }
        if (!status.isBlank()) {
            return status;
        }

        // Many PayOS webhook payloads use code=00 to indicate a successful paid event.
        String dataCode = Objects.toString(data.get("code"), "").trim();
        String topCode = Objects.toString(top.get("code"), "").trim();
        if ("00".equals(dataCode) || "00".equals(topCode)) {
            return "PAID";
        }

        String transactionDateTime = Objects.toString(data.get("transactionDateTime"), "").trim();
        if (!transactionDateTime.isBlank()) {
            return "PAID";
        }
        return "";
    }

    private String buildSafePayOsItemName(Subject subject) {
        String raw = subject == null ? "" : String.valueOf(subject.getName());
        String cleaned = raw
                .replaceAll("[^A-Za-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isBlank()) {
            String code = subject == null ? "" : String.valueOf(subject.getCode());
            cleaned = code == null ? "" : code.replaceAll("[^A-Za-z0-9]", "");
        }
        if (cleaned.isBlank()) {
            long id = subject == null || subject.getId() == null ? 0L : subject.getId();
            cleaned = "Subject " + id;
        }
        return trimForPayOs(cleaned, 25);
    }
}
