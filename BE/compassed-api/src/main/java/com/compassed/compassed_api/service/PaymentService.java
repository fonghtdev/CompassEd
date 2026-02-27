package com.compassed.compassed_api.service;

import com.compassed.compassed_api.domain.entity.Payment;
import com.compassed.compassed_api.domain.entity.Subscription;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.repository.PaymentRepository;
import com.compassed.compassed_api.repository.SubscriptionRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    
    @Value("${vnpay.tmn-code:DEMO}")
    private String vnpTmnCode;
    
    @Value("${vnpay.hash-secret:DEMO_SECRET_KEY}")
    private String vnpHashSecret;
    
    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpUrl;
    
    @Value("${vnpay.return-url:http://localhost:3000/payment/callback}")
    private String vnpReturnUrl;
    
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
                createSubscriptionAfterPayment(payment);
                
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
        
        return Map.of(
            "paymentId", payment.getId(),
            "status", payment.getStatus(),
            "amount", payment.getAmount(),
            "createdAt", payment.getCreatedAt(),
            "confirmedAt", payment.getConfirmedAt() != null ? payment.getConfirmedAt() : ""
        );
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
    private void createSubscriptionAfterPayment(Payment payment) {
        User user = userRepository.findById(payment.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        Subject subject = subjectRepository.findById(payment.getSubjectId())
            .orElseThrow(() -> new RuntimeException("Subject not found"));
        
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setSubject(subject);
        subscription.setActive(true);
        subscription.setActivatedAt(LocalDateTime.now());
        
        subscriptionRepository.save(subscription);
        log.info("Created subscription for userId={}, subjectId={}", payment.getUserId(), payment.getSubjectId());
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
}
