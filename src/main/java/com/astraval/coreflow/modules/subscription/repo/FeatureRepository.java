package com.astraval.coreflow.modules.subscription.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.subscription.model.Feature;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Long> {

    Optional<Feature> findByFeatureCodeAndIsActiveTrue(String featureCode);

    List<Feature> findByIsActiveTrue();
}
