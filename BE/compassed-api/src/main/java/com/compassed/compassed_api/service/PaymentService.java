package com.compassed.compassed_api.service;

import com.compassed.compassed_api.domain.entity.Payment;
import com.compassed.compassed_api.domain.entity.PaymentSubjectItem;
import com.compassed.compassed_api.domain.entity.Subscription;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.repository.PaymentRepository;
import com.compassed.compassed_api.repository.PaymentSubjectItemRepository;
import com.compassed.compassed_api.repository.SubscriptionRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.*;
import java.util.stream.Collectors;

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

    @Value("${casso.enabled:false}")
    private boolean cassoEnabled;

    @Value("${casso.api-key:}")
    private String cassoApiKey;

    @Value("${casso.base-url:https://oauth.casso.vn/v2}")
    private String cassoBaseUrl;

    @Value("${casso.account-number:}")
    private String cassoAccountNumber;

    @Value("${casso.page-size:50}")
    private int cassoPageSize;

    @Value("${casso.check-cooldown-seconds:20}")
    private long cassoCheckCooldownSeconds;
    
    /**
     * Tạo payment record và generate VNPay URL
     */
    @Transactional
    public Map<String, Object> createPayment(Long userId, Long subjectId, String packageType) {
        // Tính amount dựa vào package type
        BigDecimal amount = calculateAmount(packageType);
        
        // Create payment record
        Payment payment = new Payment(userId, amount, "VNPAY", subjectId, packageType);
        payment = paymentRepository.save(payment);
        
        // Generate VNPay URL
        String paymentUrl = generateVNPayUrl(payment);
        
        return Map.of(
            "paymentId", payment.getId(),
            "amount", amount,
            "currency", "VND",
            "paymentUrl", paymentUrl,
            "status", "PENDING"
        );
    }
    
    /**
     * Verify VNPay callback
     */
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
                // Extract paymentId from transactionId (format: PAY{paymentId})
                String paymentIdStr = transactionId.replace("PAY", "");
                Long paymentId = Long.parseLong(paymentIdStr);
                paymentOpt = paymentRepository.findById(paymentId);
            }
            
            if (paymentOpt.isEmpty()) {
                return Map.of("success", false, "message", "Payment not found");
            }
            
            Payment payment = paymentOpt.get();
            
            if ("00".equals(responseCode)) {
                // Payment success
                payment.setStatus("SUCCESS");
                payment.setTransactionId(params.get("vnp_TransactionNo"));
                payment.setConfirmedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                
                // Create subscription
                createSubscriptionAfterPayment(payment.getUserId(), payment.getSubjectId());
                
                return Map.of(
                    "success", true,
                    "message", "Payment successful",
                    "paymentId", payment.getId(),
                    "amount", payment.getAmount()
                );
            } else {
                // Payment failed
                payment.setStatus("FAILED");
                paymentRepository.save(payment);
                
                return Map.of("success", false, "message", "Payment failed: " + responseCode);
            }
            
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    /**
     * Get payment status
     */
    public Map<String, Object> getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        return paymentStatusPayload(payment);
    }

    public Map<String, Object> getPaymentStatusForUser(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment = maybeAutoConfirmWithCasso(payment);
        return paymentStatusPayload(payment);
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

        List<Subject> subjects = subjectRepository.findAllById(subjectIds);
        if (subjects.size() != subjectIds.size()) {
            throw new RuntimeException("Some subjects not found");
        }

        long totalAmountVnd = pricingService.calculateTotalAmountVnd(subjectIds.size());

        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAmount(BigDecimal.valueOf(totalAmountVnd));
        payment.setCurrency("VND");
        payment.setPaymentMethod("BANK_TRANSFER");
        payment.setPaymentGateway("CASSO_QR");
        payment.setPackageType("SUBJECT_BUNDLE_" + subjectIds.size());
        payment.setStatus("PENDING");
        Payment persistedPayment = paymentRepository.save(payment);
        String paymentReference = buildPaymentReference(persistedPayment.getId());
        persistedPayment.setPaymentReference(paymentReference);
        persistedPayment.setTransferNote(paymentReference);
        persistedPayment = paymentRepository.save(persistedPayment);
        Payment paymentForItems = persistedPayment;

        List<PaymentSubjectItem> items = subjectIds.stream().map(subjectId -> {
            PaymentSubjectItem item = new PaymentSubjectItem();
            item.setPayment(paymentForItems);
            item.setSubjectId(subjectId);
            return item;
        }).toList();
        paymentSubjectItemRepository.saveAll(items);

        Map<String, Object> payload = paymentStatusPayload(persistedPayment);
        payload.put("subjectIds", subjectIds);
        payload.put("subjectCount", subjectIds.size());
        payload.put("qrImageUrl", buildCheckoutQrUrl(totalAmountVnd, paymentReference));
        payload.put("bankName", checkoutQrBank);
        payload.put("bankBin", checkoutQrBankBin);
        payload.put("accountNo", checkoutQrAccountNo);
        payload.put("accountName", checkoutQrAccountName);
        payload.put("transferContent", paymentReference);
        return payload;
    }

    @Transactional
    public Map<String, Object> submitTransfer(Long userId, Long paymentId, String transferNote) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            return paymentStatusPayload(payment);
        }

        if (!"PENDING".equalsIgnoreCase(payment.getStatus()) && !"SUBMITTED".equalsIgnoreCase(payment.getStatus())) {
            throw new RuntimeException("Cannot submit transfer for status=" + payment.getStatus());
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

    private Payment maybeAutoConfirmWithCasso(Payment payment) {
        String status = String.valueOf(payment.getStatus()).toUpperCase();
        if (!"PENDING".equals(status) && !"SUBMITTED".equals(status)) {
            return payment;
        }
        if (!cassoEnabled || cassoApiKey == null || cassoApiKey.isBlank()) {
            return payment;
        }
        LocalDateTime lastCheckedAt = payment.getLastCheckedAt();
        if (lastCheckedAt != null && lastCheckedAt.isAfter(LocalDateTime.now().minusSeconds(cassoCheckCooldownSeconds))) {
            return payment;
        }

        payment.setLastCheckedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        try {
            Optional<Map<String, Object>> matchedTx = findMatchingCassoTransaction(payment);
            if (matchedTx.isEmpty()) {
                return payment;
            }
            Map<String, Object> tx = matchedTx.get();
            String txId = Objects.toString(tx.get("id"), null);
            if (txId == null || txId.isBlank()) {
                txId = Objects.toString(tx.get("tid"), null);
            }
            if (txId == null || txId.isBlank()) {
                txId = Objects.toString(tx.get("referenceNum"), null);
            }

            List<Long> subjectIds = resolveSubjectIds(payment);
            for (Long subjectId : subjectIds) {
                createSubscriptionAfterPayment(payment.getUserId(), subjectId);
            }
            payment.setStatus("SUCCESS");
            payment.setConfirmedAt(LocalDateTime.now());
            if (txId != null) {
                payment.setTransactionId(txId);
            }
            payment = paymentRepository.save(payment);
            return payment;
        } catch (Exception ex) {
            log.warn("Casso check failed for paymentId={}: {}", payment.getId(), ex.getMessage());
            return payment;
        }
    }

    private Optional<Map<String, Object>> findMatchingCassoTransaction(Payment payment) {
        String paymentRef = payment.getPaymentReference();
        if (paymentRef == null || paymentRef.isBlank()) {
            return Optional.empty();
        }

        String fromDate = payment.getCreatedAt() == null
                ? LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : payment.getCreatedAt().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String toDate = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        StringBuilder url = new StringBuilder(cassoBaseUrl);
        if (!cassoBaseUrl.endsWith("/")) {
            url.append('/');
        }
        url.append("transactions?page=1&pageSize=").append(Math.max(10, cassoPageSize))
                .append("&sort=DESC")
                .append("&fromDate=").append(encodeQueryValue(fromDate))
                .append("&toDate=").append(encodeQueryValue(toDate));
        if (cassoAccountNumber != null && !cassoAccountNumber.isBlank()) {
            url.append("&bankSubAccId=").append(encodeQueryValue(cassoAccountNumber));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Apikey " + cassoApiKey.trim());
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, Map.class);
        Map<?, ?> body = response.getBody();
        if (body == null) {
            return Optional.empty();
        }

        Object data = body.get("data");
        if (!(data instanceof Map<?, ?> dataMap)) {
            return Optional.empty();
        }
        Object recordsObj = dataMap.get("records");
        if (!(recordsObj instanceof List<?> records)) {
            return Optional.empty();
        }

        long expectedAmount = payment.getAmount() == null ? 0L : payment.getAmount().longValue();
        for (Object recordObj : records) {
            if (!(recordObj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> record = raw.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> String.valueOf(e.getKey()),
                            Map.Entry::getValue,
                            (a, b) -> a,
                            LinkedHashMap::new));

            long amount = parseLong(record.get("amount"));
            if (amount <= 0 || amount != expectedAmount) {
                continue;
            }
            String description = Objects.toString(
                    firstNonNull(record.get("description"), record.get("desc"), record.get("content")), "");
            if (description.toUpperCase().contains(paymentRef.toUpperCase())) {
                return Optional.of(record);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Generate VNPay payment URL
     */
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
    
    /**
     * Create subscription after successful payment
     */
    private void createSubscriptionAfterPayment(Long userId, Long subjectId) {
        if (subjectId == null) {
            return;
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new RuntimeException("Subject not found"));

        Optional<Subscription> existingOpt = subscriptionRepository.findByUser_IdAndSubject_Id(userId, subjectId);
        if (existingOpt.isPresent() && Boolean.TRUE.equals(existingOpt.get().isActive())) {
            return;
        }

        Subscription subscription = existingOpt.orElseGet(Subscription::new);
        subscription.setUser(user);
        subscription.setSubject(subject);
        subscription.setActive(true);
        subscription.setActivatedAt(LocalDateTime.now());

        subscriptionRepository.save(subscription);
        log.info("Created subscription for userId={}, subjectId={}", userId, subjectId);
    }
    
    /**
     * Calculate amount based on package type
     */
    private BigDecimal calculateAmount(String packageType) {
        return switch (packageType) {
            case "PLACEMENT_PACK" -> new BigDecimal("299000"); // 299k for placement pack
            case "SUBSCRIPTION_MONTHLY" -> new BigDecimal("499000"); // 499k per month
            case "SUBSCRIPTION_3MONTHS" -> new BigDecimal("1299000"); // 1.299M for 3 months
            case "SUBSCRIPTION_6MONTHS" -> new BigDecimal("2499000"); // 2.499M for 6 months
            default -> new BigDecimal("299000");
        };
    }
    
    /**
     * Get subscription duration in months
     */
    private int getSubscriptionDuration(String packageType) {
        return switch (packageType) {
            case "SUBSCRIPTION_3MONTHS" -> 3;
            case "SUBSCRIPTION_6MONTHS" -> 6;
            default -> 1;
        };
    }
    
    /**
     * HMAC SHA512
     */
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
    
    /**
     * Hash all fields for signature verification
     */
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
        Payment checkedPayment = maybeAutoConfirmWithCasso(payment);
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

    private String buildPaymentReference(Long paymentId) {
        return "CE" + paymentId;
    }

    private String buildCheckoutQrUrl(long amountVnd, String paymentReference) {
        if (checkoutQrBankBin == null || checkoutQrBankBin.isBlank()
                || checkoutQrAccountNo == null || checkoutQrAccountNo.isBlank()) {
            return checkoutQrImageUrl;
        }
        String addInfo = encodeQueryValue(paymentReference);
        String accountName = encodeQueryValue(checkoutQrAccountName == null ? "COMPASSED" : checkoutQrAccountName);
        return "https://img.vietqr.io/image/" + checkoutQrBankBin.trim() + "-" + checkoutQrAccountNo.trim()
                + "-compact2.png?amount=" + amountVnd
                + "&addInfo=" + addInfo
                + "&accountName=" + accountName;
    }

    private String encodeQueryValue(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private long parseLong(Object value) {
        if (value == null) {
            return 0L;
        }
        try {
            if (value instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(String.valueOf(value).replace(",", "").trim());
        } catch (Exception ex) {
            return 0L;
        }
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
