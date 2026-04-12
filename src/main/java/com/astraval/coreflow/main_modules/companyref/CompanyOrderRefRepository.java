package com.astraval.coreflow.main_modules.companyref;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyOrderRefRepository extends JpaRepository<CompanyOrderRef, Long> {

    Optional<CompanyOrderRef> findByCompanyCompanyIdAndOrderDetailsOrderId(Long companyId, Long orderId);

    List<CompanyOrderRef> findByCompanyCompanyIdAndOrderDetailsOrderIdIn(Long companyId, List<Long> orderIds);
}
