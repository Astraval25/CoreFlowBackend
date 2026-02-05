package com.astraval.coreflow.modules.customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerVendorLinkRepository extends JpaRepository<CustomerVendorLink, Long> {

    Optional<CustomerVendorLink> findByCustomerCustomerId(Long customerId);

    Optional<CustomerVendorLink> findByCustomerCustomerIdAndVendorCompanyId(Long customerId, Long vendorCompanyId);

    Optional<CustomerVendorLink> findByVendorVendorIdAndCustomerCompanyId(Long vendorId, Long customerCompanyId);
}
