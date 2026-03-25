package com.astraval.coreflow.modules.companyref;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyPaymentRefRepository extends JpaRepository<CompanyPaymentRef, Long> {

    Optional<CompanyPaymentRef> findByCompanyCompanyIdAndPaymentPaymentId(Long companyId, Long paymentId);

    List<CompanyPaymentRef> findByCompanyCompanyIdAndPaymentPaymentIdIn(Long companyId, List<Long> paymentIds);
}
