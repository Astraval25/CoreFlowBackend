package com.astraval.coreflow.modules.vendor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<Vendors, Long> {
    List<Vendors> findByCompanyCompanyIdAndIsActiveTrue(Integer companyId);
}