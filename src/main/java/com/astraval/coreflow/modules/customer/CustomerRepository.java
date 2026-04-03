package com.astraval.coreflow.modules.customer;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto;

@Repository
public interface CustomerRepository extends JpaRepository<Customers, Long> {
    
    @Query("SELECT new com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto(" +
           "c.customerId, c.displayName, " +
           "COALESCE(cc.companyName, ''), c.email, c.dueAmount, c.isActive) " +
           "FROM Customers c " +
           "LEFT JOIN c.customerCompany cc " +
           "WHERE c.company.companyId = :companyId " +
           "ORDER BY c.displayName")
    List<CustomerSummaryDto> findByCompanyIdSummary(@Param("companyId") Long companyId);

    @Query("SELECT new com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto(" +
           "c.customerId, c.displayName, " +
           "COALESCE(cc.companyName, ''), c.email, c.dueAmount, c.isActive) " +
           "FROM Customers c " +
           "LEFT JOIN c.customerCompany cc " +
           "WHERE c.company.companyId = :companyId " +
           "AND c.customerCompany IS NULL " +
           "ORDER BY c.displayName")
    List<CustomerSummaryDto> findUnlinkedByCompanyIdSummary(@Param("companyId") Long companyId);
    
    @Query("SELECT new com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto(" +
           "c.customerId, c.displayName, " +
           "COALESCE(cc.companyName, ''), c.email, c.dueAmount, c.isActive) " +
           "FROM Customers c " +
           "LEFT JOIN c.customerCompany cc " +
           "WHERE c.company.companyId = :companyId " +
           "AND COALESCE(c.isActive, FALSE) = :isActive " +
           "ORDER BY c.displayName")
    List<CustomerSummaryDto> findByCompanyCompanyIdAndIsActiveOrderByDisplayName(
                  @Param("companyId") Long companyId, @Param("isActive") Boolean isActive);
    
    List<Customers> findByCompanyCompanyIdOrderByDisplayName(Long companyId);

    Optional<Customers> findByCustomerIdAndCompanyCompanyId(
                  Long customerId,
                  Long companyId);


    Optional<Customers> findByCompanyCompanyIdAndCustomerCompanyCompanyId(Long companyId, Long customerCompanyId);

    @Query(value = "SELECT COALESCE(fn_customer_due_amount(:customerId), 0.0)", nativeQuery = true)
    Double calculateDueAmount(@Param("customerId") Long customerId);

    @Query(value = """
           SELECT
             COALESCE((
               SELECT SUM(COALESCE(o.total_amount, 0.0))
               FROM order_details o
               WHERE o.customer = :customerId
                 AND COALESCE(o.is_active, TRUE) = TRUE
                 AND COALESCE(o.order_status, '') NOT IN (
                   'QUOTATION',
                   'QUOTATION_VIEWED',
                   'QUOTATION_ACCEPTED',
                   'QUOTATION_DECLINED'
                 )
             ), 0.0)
             -
             COALESCE((
               SELECT SUM(COALESCE(p.amount, 0.0))
               FROM payments p
               WHERE p.customer = :customerId
                 AND COALESCE(p.is_active, TRUE) = TRUE
                 AND COALESCE(p.payment_status, '') <> 'PAYMENT_DECLINED'
             ), 0.0)
           """, nativeQuery = true)
    Double calculateDueAmountFallback(@Param("customerId") Long customerId);

    @Modifying
    @Transactional
    @Query("UPDATE Customers c SET c.dueAmount = :dueAmount WHERE c.customerId = :customerId")
    int updateDueAmount(@Param("customerId") Long customerId, @Param("dueAmount") Double dueAmount);

    @Query(value = """
            SELECT o.order_id, o.order_number, o.total_amount,
                   o.platform_ref, o.paid_amount, o.order_date
            FROM order_details o
            WHERE o.customer = :customerId
              AND COALESCE(o.is_active, TRUE) = TRUE
              AND (:search IS NULL OR :search = ''
                   OR o.order_number ILIKE CONCAT('%', :search, '%')
                   OR o.platform_ref ILIKE CONCAT('%', :search, '%')
                   OR CAST(o.total_amount AS VARCHAR) ILIKE CONCAT('%', :search, '%'))
            ORDER BY o.order_date DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM order_details o
            WHERE o.customer = :customerId AND COALESCE(o.is_active, TRUE) = TRUE
              AND (:search IS NULL OR :search = ''
                   OR o.order_number ILIKE CONCAT('%', :search, '%')
                   OR o.platform_ref ILIKE CONCAT('%', :search, '%')
                   OR CAST(o.total_amount AS VARCHAR) ILIKE CONCAT('%', :search, '%'))
            """,
            nativeQuery = true)
    Page<Object[]> findOrdersByCustomerId(
            @Param("customerId") Long customerId,
            @Param("search") String search,
            Pageable pageable);

    @Query(value = """
            SELECT p.payment_id, p.platform_ref, p.payment_date, p.amount
            FROM payments p
            WHERE p.customer = :customerId
              AND COALESCE(p.is_active, TRUE) = TRUE
              AND (:search IS NULL OR :search = ''
                   OR p.platform_ref ILIKE CONCAT('%', :search, '%')
                   OR CAST(p.amount AS VARCHAR) ILIKE CONCAT('%', :search, '%'))
            ORDER BY p.payment_date DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM payments p
            WHERE p.customer = :customerId AND COALESCE(p.is_active, TRUE) = TRUE
              AND (:search IS NULL OR :search = ''
                   OR p.platform_ref ILIKE CONCAT('%', :search, '%')
                   OR CAST(p.amount AS VARCHAR) ILIKE CONCAT('%', :search, '%'))
            """,
            nativeQuery = true)
    Page<Object[]> findPaymentsByCustomerId(
            @Param("customerId") Long customerId,
            @Param("search") String search,
            Pageable pageable);

}
