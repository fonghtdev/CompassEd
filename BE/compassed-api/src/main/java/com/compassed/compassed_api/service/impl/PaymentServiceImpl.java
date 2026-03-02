package com.compassed.compassed_api.service.impl;

import com.compassed.compassed_api.api.dto.request.PaymentCallbackRequest;
import com.compassed.compassed_api.api.dto.request.PaymentCreateRequest;
import com.compassed.compassed_api.api.dto.response.PaymentCallbackResponse;
import com.compassed.compassed_api.api.dto.response.PaymentCreateResponse;
import com.compassed.compassed_api.domain.entity.Package;
import com.compassed.compassed_api.domain.entity.Payment;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.entity.Subscription;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.enums.PaymentMethod;
import com.compassed.compassed_api.domain.enums.PaymentStatus;
import com.compassed.compassed_api.repository.PackageRepository;
import com.compassed.compassed_api.repository.PaymentRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.SubscriptionRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {
    
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PackageRepository packageRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    
    @Override
    @Transactional
    public PaymentCreateResponse createPayment(PaymentCreateRequest request) {
        // Validate user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate subject
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        
        // Validate package
        Package pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        if (!pkg.getIsActive()) {
            throw new RuntimeException("Package is not active");
        }
        
        // Validate payment method
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment method");
        }
        
        // Generate transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString();
        
        // Create payment
        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .subjectId(request.getSubjectId())
                .packageId(request.getPackageId())
                .amount(request.getAmount())
                .paymentMethod(paymentMethod)
                .status(PaymentStatus.PENDING)
                .transactionId(transactionId)
                .paymentUrl(generatePaymentUrl(paymentMethod, transactionId, request.getAmount()))
                .build();
        
        payment = paymentRepository.save(payment);
        
        return PaymentCreateResponse.builder()
                .paymentId(payment.getId())
                .transactionId(payment.getTransactionId())
                .paymentUrl(payment.getPaymentUrl())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .build();
    }
    
    @Override
    @Transactional
    public PaymentCallbackResponse handleCallback(PaymentCallbackRequest request) {
        // Find payment by transaction ID
        Payment payment = paymentRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        String paymentStatus = request.getPaymentStatus().toUpperCase();
        
        if ("SUCCESS".equals(paymentStatus)) {
            // Update payment status
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            
            // Get package details
            Package pkg = packageRepository.findById(payment.getPackageId())
                    .orElseThrow(() -> new RuntimeException("Package not found"));
            
            // Create subscription
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(pkg.getDurationDays());
            
            Subscription subscription = Subscription.builder()
                    .userId(payment.getUserId())
                    .subjectId(payment.getSubjectId())
                    .packageId(payment.getPackageId())
                    .paymentId(payment.getId())
                    .startDate(startDate)
                    .endDate(endDate)
                    .isActive(true)
                    .placementUnlocked(pkg.getIsPlacementPackage())
                    .build();
            
            subscription = subscriptionRepository.save(subscription);
            
            return PaymentCallbackResponse.builder()
                    .transactionId(payment.getTransactionId())
                    .status("SUCCESS")
                    .message("Payment successful. Subscription activated.")
                    .subscriptionId(subscription.getId())
                    .placementUnlocked(subscription.getPlacementUnlocked())
                    .build();
            
        } else if ("FAILED".equals(paymentStatus)) {
            // Update payment status to FAILED
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            
            return PaymentCallbackResponse.builder()
                    .transactionId(payment.getTransactionId())
                    .status("FAILED")
                    .message("Payment failed.")
                    .subscriptionId(null)
                    .placementUnlocked(false)
                    .build();
        } else {
            throw new RuntimeException("Invalid payment status");
        }
    }
    
    private String generatePaymentUrl(PaymentMethod method, String transactionId, Double amount) {
        // Mock payment URL generation
        String baseUrl = switch (method) {
            case VNPAY -> "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
            case MOMO -> "https://test-payment.momo.vn/gw_payment/transactionProcessor";
            case STRIPE -> "https://checkout.stripe.com/pay";
        };
        
        return baseUrl + "?txnId=" + transactionId + "&amount=" + amount;
    }
}
