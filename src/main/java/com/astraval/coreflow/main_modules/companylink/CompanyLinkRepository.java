package com.astraval.coreflow.main_modules.companylink;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyLinkRepository extends JpaRepository<CompanyLink, Long> {

    Optional<CompanyLink> findByCustomerCustomerId(Long customerId);

    Optional<CompanyLink> findByCustomerCustomerIdAndVendorCompanyCompanyId(Long customerId, Long vendorCompanyId);

    Optional<CompanyLink> findByVendorVendorIdAndCustomerCompanyCompanyId(Long vendorId, Long customerCompanyId);

    Optional<CompanyLink> findByVendorVendorIdAndVendorCompanyCompanyId(Long vendorId, Long customerCompanyId);
}
