package com.compassed.compassed_api.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
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

    @Column(name = "payment_reference", length = 64)
    private String paymentReference;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;
    
    @Column(nullable = false, length = 50)
    private String status; // PENDING, SUCCESS, FAILED, CANCELLED
    
    @Column(name = "subject_id")
    private Long subjectId;
    
    @Column(name = "package_type", length = 50)
    private String packageType; // PLACEMENT_PACK, SUBSCRIPTION_MONTHLY, etc.

    @Column(name = "transfer_note", length = 255)
    private String transferNote;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}
