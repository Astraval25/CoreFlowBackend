package com.astraval.coreflow.modules.payments.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.payments.dto.PaymentOrderAllocationDto;
import com.astraval.coreflow.modules.payments.model.Payments;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, Long> {

    @Query(value = """
            SELECT 
              p.payment_id,
              p.payment_date,
              STRING_AGG(poa.order_id::text, ', ') AS order_ids,
              p.payment_number,
              p.amount,
              v.vendor_name,
              p.mode_of_payment,
              p.payment_status,
              p.is_active,
              p.reference_number 
            FROM payments p
            LEFT JOIN payment_order_allocations poa ON p.payment_id = poa.payment_id
            LEFT JOIN vendors v ON v.vendor_id = p.vendor
            WHERE p.buyer_company = :companyId
            GROUP BY 
              p.payment_id, p.payment_date, p.payment_number, p.amount,
              v.vendor_name, p.mode_of_payment, p.payment_status, p.is_active, p.reference_number
            ORDER BY p.payment_date DESC
            """, nativeQuery = true)
    List<Object[]> findPayerPaymentSummaryByCompanyIdNative(@Param("companyId") Long companyId);

    @Query(value = """
            SELECT 
              p.payment_id,
              p.payment_date,
              STRING_AGG(poa.order_id::text, ', ') AS order_ids,
              p.payment_number,
              p.amount,
              c.customer_name,
              p.mode_of_payment,
              p.payment_status,
              p.is_active,
              p.reference_number 
            FROM payments p
            LEFT JOIN payment_order_allocations poa ON p.payment_id = poa.payment_id
            LEFT JOIN customers c ON c.customer_id = p.customer
            WHERE p.seller_company = :companyId
            GROUP BY 
              p.payment_id, p.payment_date, p.payment_number, p.amount,
              c.customer_name, p.mode_of_payment, p.payment_status, p.is_active, p.reference_number
            ORDER BY p.payment_date DESC
            """, nativeQuery = true)
    List<Object[]> findPayeePaymentSummaryByCompanyIdNative(@Param("companyId") Long companyId);

    @Query("""
            SELECT p FROM Payments p
            LEFT JOIN FETCH p.buyerCompany
            LEFT JOIN FETCH p.sellerCompany
            LEFT JOIN FETCH p.vendors
            LEFT JOIN FETCH p.customers
            WHERE p.paymentId = :paymentId
            """)
    Optional<Payments> findPaymentWithDetailsById(@Param("paymentId") Long paymentId);

    @Query("""
            SELECT new com.astraval.coreflow.modules.payments.dto.PaymentOrderAllocationDto(
                poa.paymentOrderAllocationId,
                poa.orderDetails.orderId,
                poa.orderDetails.orderNumber,
                poa.amountApplied,
                poa.allocationDate,
                poa.allocationRemarks,
                poa.isActive
            )
            FROM PaymentOrderAllocations poa
            WHERE poa.payments.paymentId = :paymentId
            ORDER BY poa.allocationDate DESC
            """)
    List<PaymentOrderAllocationDto> findPaymentOrderAllocationsByPaymentId(@Param("paymentId") Long paymentId);

}
