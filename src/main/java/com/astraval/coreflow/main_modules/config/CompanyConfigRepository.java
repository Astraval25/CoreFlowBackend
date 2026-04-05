package com.astraval.coreflow.main_modules.config;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyConfigRepository extends JpaRepository<CompanyConfig, Long> {

    Optional<CompanyConfig> findByCompanyCompanyIdAndConfigKey(Long companyId, String configKey);

    List<CompanyConfig> findByCompanyCompanyId(Long companyId);
}
