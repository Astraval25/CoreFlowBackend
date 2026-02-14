package com.astraval.coreflow.modules.customer;

import java.util.List;
import java.util.Optional;

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
               JOIN customers c ON c.customer_id = o.customer
               WHERE c.customer_id = :customerId
                 AND COALESCE(o.is_active, TRUE) = TRUE
                 AND o.seller_company = c.comp_id
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
               JOIN customers c ON c.customer_id = p.customer
               WHERE c.customer_id = :customerId
                 AND COALESCE(p.is_active, TRUE) = TRUE
                 AND p.receiver_comp = c.comp_id
                 AND COALESCE(p.payment_status, '') <> 'PAYMENT_DECLINED'
             ), 0.0)
           """, nativeQuery = true)
    Double calculateDueAmountFallback(@Param("customerId") Long customerId);

    @Modifying
    @Transactional
    @Query("UPDATE Customers c SET c.dueAmount = :dueAmount WHERE c.customerId = :customerId")
    int updateDueAmount(@Param("customerId") Long customerId, @Param("dueAmount") Double dueAmount);

}
