package com.astraval.coreflow.modules.companies;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.companies.dto.CompanySummaryDto;

@Repository
public interface CompanyRepository extends JpaRepository<Companies, Long> {
  
    @Query("SELECT new com.astraval.coreflow.modules.companies.dto.CompanySummaryDto(" +
           "c.companyId, c.companyName, c.industry, c.shortName, c.isActive) " +
           "FROM Companies c " +
           "JOIN UserCompanyMap ucm ON c.companyId = ucm.company.companyId " +
           "WHERE ucm.user.userId = :userId " +
           "ORDER BY c.companyName")
    List<CompanySummaryDto> findCompaniesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT new com.astraval.coreflow.modules.companies.dto.CompanySummaryDto(" +
           "c.companyId, c.companyName, c.industry, c.shortName, c.isActive) " +
           "FROM Companies c " +
           "JOIN UserCompanyMap ucm ON c.companyId = ucm.company.companyId " +
           "WHERE ucm.user.userId = :userId " +
           "AND c.isActive = true " +
           "ORDER BY c.companyName")
    List<CompanySummaryDto> findActiveCompaniesByUserId(@Param("userId") Long userId);
}
