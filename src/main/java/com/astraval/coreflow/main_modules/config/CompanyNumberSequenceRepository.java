package com.astraval.coreflow.main_modules.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyNumberSequenceRepository extends JpaRepository<CompanyNumberSequence, Long> {

    @Query(value = "SELECT generate_company_number(:companyId, :numberType)", nativeQuery = true)
    String generateCompanyNumber(@Param("companyId") Long companyId, @Param("numberType") String numberType);
}
