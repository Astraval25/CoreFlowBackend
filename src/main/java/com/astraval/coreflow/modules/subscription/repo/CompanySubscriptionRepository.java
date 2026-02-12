package com.astraval.coreflow.modules.subscription.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.subscription.model.CompanySubscription;

@Repository
public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Long> {

    Optional<CompanySubscription> findTopByCompanyCompanyIdOrderByEndAtDesc(Long companyId);

    Optional<CompanySubscription> findByProviderSubscriptionId(String providerSubscriptionId);
}
