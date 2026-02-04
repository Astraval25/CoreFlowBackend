package com.astraval.coreflow.modules.customer;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto;

@Repository
public interface CustomerRepository extends JpaRepository<Customers, Long> {
    
    @Query("SELECT new com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto(" +
           "c.customerId, c.displayName, " +
           "COALESCE(cc.companyName, ''), c.email, c.isActive) " +
           "FROM Customers c " +
           "LEFT JOIN c.customerCompany cc " +
           "WHERE c.company.companyId = :companyId " +
           "ORDER BY c.displayName")
    List<CustomerSummaryDto> findByCompanyIdSummary(@Param("companyId") Long companyId);

    @Query("SELECT new com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto(" +
           "c.customerId, c.displayName, " +
           "COALESCE(cc.companyName, ''), c.email, c.isActive) " +
           "FROM Customers c " +
           "LEFT JOIN c.customerCompany cc " +
           "WHERE c.company.companyId = :companyId " +
           "AND c.customerCompany IS NULL " +
           "ORDER BY c.displayName")
    List<CustomerSummaryDto> findUnlinkedByCompanyIdSummary(@Param("companyId") Long companyId);
    
    @Query("SELECT new com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto(" +
           "c.customerId, c.displayName, " +
           "COALESCE(cc.companyName, ''), c.email, c.isActive) " +
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

}
