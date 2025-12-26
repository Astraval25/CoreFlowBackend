package com.astraval.coreflow.modules.customer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto;

@Repository
public interface CustomerRepository extends JpaRepository<Customers, Long> {
    
    @Query("SELECT new com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto(" +
           "c.customerId, c.displayName, " +
           "COALESCE(cc.companyName, ''), c.email) " +
           "FROM Customers c " +
           "LEFT JOIN c.customerCompany cc " +
           "WHERE c.company.companyId = :companyId " +
           "ORDER BY c.displayName")
    List<CustomerSummaryDto> findByCompanyIdSummary(@Param("companyId") Long companyId);
    
    List<Customers> findByCompanyCompanyIdAndIsActiveOrderByDisplayName(Long companyId, Boolean isActive);
    
    List<Customers> findByCompanyCompanyIdOrderByDisplayName(Long companyId);
}
