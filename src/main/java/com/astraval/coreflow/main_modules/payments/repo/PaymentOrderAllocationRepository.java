package com.astraval.coreflow.main_modules.payments.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.main_modules.payments.model.PaymentOrderAllocations;
import com.astraval.coreflow.main_modules.payments.model.Payments;

@Repository
public interface PaymentOrderAllocationRepository extends JpaRepository<PaymentOrderAllocations, Long> {

    boolean existsByOrderDetailsOrderId(Long orderId);

    List<PaymentOrderAllocations> findByPayments(Payments payment);
    
    void deleteByPayments(Payments payment);
    
    @Query("""
            SELECT COALESCE(SUM(poa.amountApplied), 0.0)
            FROM PaymentOrderAllocations poa
            WHERE poa.orderDetails.orderId = :orderId
              AND COALESCE(poa.isActive, true) = true
              AND COALESCE(poa.payments.isActive, true) = true
            """)
    Double getTotalPaidAmountForOrder(@Param("orderId") Long orderId);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentOrderAllocations poa SET poa.isActive = :isActive WHERE poa.orderDetails.orderId = :orderId")
    int updateIsActiveByOrderId(@Param("orderId") Long orderId, @Param("isActive") Boolean isActive);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentOrderAllocations poa SET poa.isActive = :isActive WHERE poa.payments.paymentId = :paymentId")
    int updateIsActiveByPaymentId(@Param("paymentId") Long paymentId, @Param("isActive") Boolean isActive);
}
