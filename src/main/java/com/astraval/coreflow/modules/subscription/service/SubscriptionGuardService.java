package com.astraval.coreflow.modules.subscription.service;

public interface SubscriptionGuardService {

    void assertActiveSubscription(Long companyId);

    void assertFeatureAccess(Long companyId, String featureCode);
}
