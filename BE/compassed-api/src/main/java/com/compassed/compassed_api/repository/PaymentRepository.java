package com.compassed.compassed_api.repository;

import com.compassed.compassed_api.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByPaymentReference(String paymentReference);
    Optional<Payment> findByPaymentReferenceAndUserId(String paymentReference, Long userId);
    Optional<Payment> findByIdAndUserId(Long id, Long userId);
    List<Payment> findByUserIdAndStatusInOrderByIdDesc(Long userId, List<String> statuses);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findByStatus(String status);
    List<Payment> findByStatusOrderByCreatedAtDesc(String status);
    List<Payment> findAllByOrderByCreatedAtDesc();

    long countByUserIdAndStatus(Long userId, String status);
}
