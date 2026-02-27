package com.compassed.compassed_api.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(length = 10)
    private String currency = "VND";
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway;
    
    @Column(name = "transaction_id", length = 255)
    private String transactionId;
    
    @Column(nullable = false, length = 50)
    private String status; // PENDING, SUCCESS, FAILED, CANCELLED
    
    @Column(name = "subject_id")
    private Long subjectId;
    
    @Column(name = "package_type", length = 50)
    private String packageType; // PLACEMENT_PACK, SUBSCRIPTION_MONTHLY, etc.
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructor
    public Payment() {}
    
    public Payment(Long userId, BigDecimal amount, String paymentGateway, Long subjectId, String packageType) {
        this.userId = userId;
        this.amount = amount;
        this.currency = "VND";
        this.paymentGateway = paymentGateway;
        this.subjectId = subjectId;
        this.packageType = packageType;
        this.status = "PENDING";
    }
}
