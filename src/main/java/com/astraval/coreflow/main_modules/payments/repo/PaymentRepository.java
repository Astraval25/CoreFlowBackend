package com.astraval.coreflow.main_modules.payments.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.main_modules.payments.dto.PayerPaymentSummaryDto;
import com.astraval.coreflow.main_modules.payments.dto.PaymentOrderAllocationDto;
import com.astraval.coreflow.main_modules.payments.model.Payments;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, Long> {

    @Query(value = "SELECT generate_payment_number(?1)", nativeQuery = true)
    String generatePaymentNumber(@Param("companyId") Long companyId);

    @Query(value = "SELECT generate_platform_payment_ref()", nativeQuery = true)
    String generatePlatformPaymentRef();

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
              p.reference_number,
              p.platform_ref,
              cpr.local_payment_number
            FROM payments p
            LEFT JOIN payment_order_allocations poa ON p.payment_id = poa.payment_id
            LEFT JOIN vendors v ON v.vendor_id = p.vendor
            LEFT JOIN company_payment_ref cpr ON cpr.payment_id = p.payment_id AND cpr.company_id = :companyId
            WHERE v.comp_id = :companyId
            GROUP BY
              p.payment_id, p.payment_date, p.payment_number, p.amount,
              v.vendor_name, p.mode_of_payment, p.payment_status, p.is_active, p.reference_number, p.platform_ref,
              cpr.local_payment_number
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
              p.reference_number,
              p.platform_ref,
              cpr.local_payment_number
            FROM payments p
            LEFT JOIN payment_order_allocations poa ON p.payment_id = poa.payment_id
            LEFT JOIN customers c ON c.customer_id = p.customer
            LEFT JOIN company_payment_ref cpr ON cpr.payment_id = p.payment_id AND cpr.company_id = :companyId
            WHERE c.comp_id = :companyId
            GROUP BY
              p.payment_id, p.payment_date, p.payment_number, p.amount,
              c.customer_name, p.mode_of_payment, p.payment_status, p.is_active, p.reference_number, p.platform_ref,
              cpr.local_payment_number
            ORDER BY p.payment_date DESC
            """, nativeQuery = true)
    List<Object[]> findPayeePaymentSummaryByCompanyIdNative(@Param("companyId") Long companyId);

    @Query("""
            SELECT p FROM Payments p
            LEFT JOIN FETCH p.customers c
            LEFT JOIN FETCH c.company
            LEFT JOIN FETCH p.vendors v
            LEFT JOIN FETCH v.company
            LEFT JOIN FETCH p.paymentProofFile
            WHERE p.paymentId = :paymentId
            """)
    Optional<Payments> findPaymentWithDetailsById(@Param("paymentId") Long paymentId);

    @Query("""
            SELECT new com.astraval.coreflow.main_modules.payments.dto.PaymentOrderAllocationDto(
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

    @Query("""
            SELECT DISTINCT new com.astraval.coreflow.main_modules.payments.dto.PayerPaymentSummaryDto(
              p.paymentId,
              p.paymentDate,
              CONCAT('', poa.orderDetails.orderId),
              p.paymentNumber,
              p.amount,
              v.vendorName,
              p.modeOfPayment,
              p.paymentStatus,
              p.isActive,
              p.referenceNumber,
              p.platformRef,
              cpr.localPaymentNumber
            )
            FROM PaymentOrderAllocations poa
            JOIN poa.payments p
            LEFT JOIN p.vendors v
            LEFT JOIN CompanyPaymentRef cpr
              ON cpr.payment = p AND cpr.company.companyId = :companyId
            WHERE poa.orderDetails.orderId = :orderId
            ORDER BY p.paymentDate DESC
            """)
    List<PayerPaymentSummaryDto> findPaymentDetailsForOrder(
            @Param("companyId") Long companyId,
            @Param("orderId") Long orderId);

}
