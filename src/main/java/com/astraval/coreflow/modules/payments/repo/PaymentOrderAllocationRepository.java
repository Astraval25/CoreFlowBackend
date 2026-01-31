package com.astraval.coreflow.modules.payments.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.payments.model.PaymentOrderAllocations;
import com.astraval.coreflow.modules.payments.model.Payments;

@Repository
public interface PaymentOrderAllocationRepository extends JpaRepository<PaymentOrderAllocations, Long> {

    boolean existsByOrderDetailsOrderId(Long orderId);
    
    void deleteByPayments(Payments payment);
    
    @Query("SELECT COALESCE(SUM(poa.amountApplied), 0.0) FROM PaymentOrderAllocations poa WHERE poa.orderDetails.orderId = :orderId")
    Double getTotalPaidAmountForOrder(@Param("orderId") Long orderId);
}