package com.astraval.coreflow.main_modules.companies;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.main_modules.companies.dto.CompanySummaryDto;

@Repository
public interface CompanyRepository extends JpaRepository<Companies, Long> {

    @Query("SELECT new com.astraval.coreflow.main_modules.companies.dto.CompanySummaryDto(" +
           "c.companyId, c.companyName, c.industry, c.shortName, c.isActive, c.fsId) " +
           "FROM Companies c " +
           "JOIN UserCompanyMap ucm ON c.companyId = ucm.company.companyId " +
           "WHERE ucm.user.userId = :userId " +
           "ORDER BY c.companyName")
    List<CompanySummaryDto> findCompaniesByUserId(@Param("userId") Long userId);

    @Query("SELECT new com.astraval.coreflow.main_modules.companies.dto.CompanySummaryDto(" +
           "c.companyId, c.companyName, c.industry, c.shortName, c.isActive, c.fsId) " +
           "FROM Companies c " +
           "JOIN UserCompanyMap ucm ON c.companyId = ucm.company.companyId " +
           "WHERE ucm.user.userId = :userId " +
           "AND c.isActive = true " +
           "ORDER BY c.companyName")
    List<CompanySummaryDto> findActiveCompaniesByUserId(@Param("userId") Long userId);

    @Query("""
           SELECT DISTINCT c
           FROM Companies c
           JOIN Items i ON i.company.companyId = c.companyId
           WHERE c.isActive = true
             AND i.isActive = true
             AND i.isSellable = true
             AND i.baseSalesPrice IS NOT NULL
           ORDER BY c.companyName
           """)
    List<Companies> findMarketplaceCompanies();
}
