package com.astraval.coreflow.modules.subscription.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.subscription.model.PlanFeature;

@Repository
public interface PlanFeatureRepository extends JpaRepository<PlanFeature, Long> {

    boolean existsByPlanPlanIdAndFeatureFeatureId(Long planId, Long featureId);

    List<PlanFeature> findByPlanPlanId(Long planId);
}
