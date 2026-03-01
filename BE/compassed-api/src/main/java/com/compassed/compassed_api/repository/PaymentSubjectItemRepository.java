package com.compassed.compassed_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.PaymentSubjectItem;

public interface PaymentSubjectItemRepository extends JpaRepository<PaymentSubjectItem, Long> {
    List<PaymentSubjectItem> findByPayment_Id(Long paymentId);
    void deleteByPayment_Id(Long paymentId);
}
