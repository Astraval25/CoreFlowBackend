package com.astraval.coreflow.modules.subscription.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.common.exception.SubscriptionAccessDeniedException;
import com.astraval.coreflow.modules.subscription.SubscriptionStatus;
import com.astraval.coreflow.modules.subscription.dto.SubscriptionCapabilitiesResponse;
import com.astraval.coreflow.modules.subscription.model.CompanySubscription;
import com.astraval.coreflow.modules.subscription.model.Feature;
import com.astraval.coreflow.modules.subscription.model.PlanFeature;
import com.astraval.coreflow.modules.subscription.repo.CompanySubscriptionRepository;
import com.astraval.coreflow.modules.subscription.repo.FeatureRepository;
import com.astraval.coreflow.modules.subscription.repo.PlanFeatureRepository;

@Service
public class SubscriptionService implements SubscriptionGuardService {

    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final FeatureRepository featureRepository;
    private final PlanFeatureRepository planFeatureRepository;

    public SubscriptionService(CompanySubscriptionRepository companySubscriptionRepository,
            FeatureRepository featureRepository,
            PlanFeatureRepository planFeatureRepository) {
        this.companySubscriptionRepository = companySubscriptionRepository;
        this.featureRepository = featureRepository;
        this.planFeatureRepository = planFeatureRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void assertActiveSubscription(Long companyId) {
        CompanySubscription subscription = getLatestSubscription(companyId);

        if (!isSubscriptionCurrentlyActive(subscription)) {
            throw new SubscriptionAccessDeniedException(
                    "Active subscription required for this operation",
                    "SUBSCRIPTION_INACTIVE",
                    null,
                    null,
                    getCurrentPlanCode(subscription),
                    "/pricing");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void assertFeatureAccess(Long companyId, String featureCode) {
        CompanySubscription subscription = getLatestSubscription(companyId);

        if (!isSubscriptionCurrentlyActive(subscription)) {
            throw new SubscriptionAccessDeniedException(
                    "Active subscription required for this feature",
                    "SUBSCRIPTION_INACTIVE",
                    featureCode,
                    null,
                    getCurrentPlanCode(subscription),
                    "/pricing");
        }

        Feature feature = featureRepository.findByFeatureCodeAndIsActiveTrue(featureCode)
                .orElseThrow(() -> new SubscriptionAccessDeniedException(
                        "Requested feature is not configured",
                        "FEATURE_NOT_ALLOWED",
                        featureCode,
                        null,
                        getCurrentPlanCode(subscription),
                        "/pricing"));

        boolean hasAccess = planFeatureRepository.existsByPlanPlanIdAndFeatureFeatureId(
                subscription.getPlan().getPlanId(),
                feature.getFeatureId());

        if (!hasAccess) {
            throw new SubscriptionAccessDeniedException(
                    "Your current plan does not allow this feature",
                    "FEATURE_NOT_ALLOWED",
                    featureCode,
                    null,
                    getCurrentPlanCode(subscription),
                    "/pricing");
        }
    }

    @Transactional(readOnly = true)
    public SubscriptionCapabilitiesResponse getCapabilities(Long companyId) {
        CompanySubscription subscription = getLatestSubscription(companyId);

        List<Feature> activeFeatures = featureRepository.findByIsActiveTrue();
        Map<String, Boolean> features = new LinkedHashMap<>();
        Map<String, SubscriptionCapabilitiesResponse.LimitInfo> limits = new LinkedHashMap<>();

        for (Feature feature : activeFeatures) {
            features.put(feature.getFeatureCode(), false);
        }

        if (subscription != null && isSubscriptionCurrentlyActive(subscription) && subscription.getPlan() != null) {
            List<PlanFeature> planFeatures = planFeatureRepository.findByPlanPlanId(subscription.getPlan().getPlanId());

            for (PlanFeature planFeature : planFeatures) {
                String featureCode = planFeature.getFeature().getFeatureCode();
                features.put(featureCode, true);

                if (planFeature.getLimitValue() != null) {
                    limits.put(featureCode,
                            new SubscriptionCapabilitiesResponse.LimitInfo(0, planFeature.getLimitValue(),
                                    planFeature.getLimitValue()));
                }
            }
        }

        SubscriptionCapabilitiesResponse.SubscriptionInfo subscriptionInfo =
                new SubscriptionCapabilitiesResponse.SubscriptionInfo(
                        getCurrentPlanCode(subscription),
                        subscription != null ? subscription.getStatus() : "INACTIVE",
                        subscription != null ? subscription.getEndAt() : null);

        return new SubscriptionCapabilitiesResponse(subscriptionInfo, features, limits);
    }

    @Transactional(readOnly = true)
    public CompanySubscription getLatestSubscription(Long companyId) {
        return companySubscriptionRepository.findTopByCompanyCompanyIdOrderByEndAtDesc(companyId).orElse(null);
    }

    public boolean isSubscriptionCurrentlyActive(CompanySubscription subscription) {
        if (subscription == null || subscription.getStatus() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (subscription.getStartAt() != null && now.isBefore(subscription.getStartAt())) {
            return false;
        }

        String status = subscription.getStatus();

        boolean baseActiveStatus = SubscriptionStatus.getActive().equals(status)
                || SubscriptionStatus.getTrial().equals(status);

        if (baseActiveStatus) {
            return subscription.getEndAt() == null || !now.isAfter(subscription.getEndAt());
        }

        if (SubscriptionStatus.getPastDue().equals(status)) {
            return subscription.getGraceUntil() != null && !now.isAfter(subscription.getGraceUntil());
        }

        return false;
    }

    private String getCurrentPlanCode(CompanySubscription subscription) {
        if (subscription == null || subscription.getPlan() == null) {
            return null;
        }
        return subscription.getPlan().getPlanCode();
    }
}
