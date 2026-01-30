package com.astraval.coreflow.modules.payments.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.payments.model.PaymentOrderAllocations;

@Repository
public interface PaymentOrderAllocationRepository extends JpaRepository<PaymentOrderAllocations, Long> {

    boolean existsByOrderDetailsOrderId(Long orderId);
}