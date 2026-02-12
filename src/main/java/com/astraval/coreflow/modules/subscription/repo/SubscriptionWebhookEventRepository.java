package com.astraval.coreflow.modules.subscription.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.subscription.model.SubscriptionWebhookEvent;

@Repository
public interface SubscriptionWebhookEventRepository extends JpaRepository<SubscriptionWebhookEvent, Long> {

    boolean existsByEventId(String eventId);
}
