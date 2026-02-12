package com.astraval.coreflow.modules.subscription.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.subscription.SubscriptionStatus;
import com.astraval.coreflow.modules.subscription.model.CompanySubscription;
import com.astraval.coreflow.modules.subscription.model.SubscriptionWebhookEvent;
import com.astraval.coreflow.modules.subscription.repo.CompanySubscriptionRepository;
import com.astraval.coreflow.modules.subscription.repo.SubscriptionWebhookEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SubscriptionWebhookService {

    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final SubscriptionWebhookEventRepository subscriptionWebhookEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.subscription.razorpay.webhook-secret:}")
    private String razorpayWebhookSecret;

    public SubscriptionWebhookService(CompanySubscriptionRepository companySubscriptionRepository,
            SubscriptionWebhookEventRepository subscriptionWebhookEventRepository) {
        this.companySubscriptionRepository = companySubscriptionRepository;
        this.subscriptionWebhookEventRepository = subscriptionWebhookEventRepository;
    }

    @Transactional
    public void processRazorpayWebhook(String signature, String payload) {
        verifySignature(signature, payload);

        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("event").asText(null);
            String eventId = root.path("id").asText(null);

            if (eventId == null || eventId.isBlank()) {
                eventId = "evt_" + System.currentTimeMillis();
            }

            if (subscriptionWebhookEventRepository.existsByEventId(eventId)) {
                return;
            }

            String providerSubscriptionId = extractProviderSubscriptionId(root);

            SubscriptionWebhookEvent webhookEvent = new SubscriptionWebhookEvent();
            webhookEvent.setEventId(eventId);
            webhookEvent.setEventType(eventType != null ? eventType : "unknown");
            webhookEvent.setProviderSubscriptionId(providerSubscriptionId);
            webhookEvent.setPayload(payload);
            webhookEvent.setIsProcessed(false);
            subscriptionWebhookEventRepository.save(webhookEvent);

            if (providerSubscriptionId != null) {
                companySubscriptionRepository.findByProviderSubscriptionId(providerSubscriptionId)
                        .ifPresent(subscription -> applyWebhookEvent(subscription, eventType, root));
            }

            webhookEvent.setIsProcessed(true);
            webhookEvent.setProcessedAt(LocalDateTime.now());
            subscriptionWebhookEventRepository.save(webhookEvent);
        } catch (Exception e) {
            throw new RuntimeException("Unable to process Razorpay webhook", e);
        }
    }

    private void verifySignature(String signature, String payload) {
        if (razorpayWebhookSecret == null || razorpayWebhookSecret.isBlank()) {
            throw new RuntimeException("Razorpay webhook secret is not configured");
        }

        if (signature == null || signature.isBlank()) {
            throw new RuntimeException("Missing Razorpay webhook signature");
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(razorpayWebhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            mac.init(keySpec);
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = toHex(digest);

            if (!expectedSignature.equalsIgnoreCase(signature)) {
                throw new RuntimeException("Invalid Razorpay webhook signature");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Razorpay webhook signature", e);
        }
    }

    private void applyWebhookEvent(CompanySubscription subscription, String eventType, JsonNode root) {
        if (eventType == null || eventType.isBlank()) {
            return;
        }

        switch (eventType) {
            case "subscription.activated" -> {
                subscription.setStatus(SubscriptionStatus.getActive());
                subscription.setStartAt(extractEpochTime(root.path("payload").path("subscription").path("entity")
                        .path("current_start"), subscription.getStartAt()));
                subscription.setEndAt(extractEpochTime(root.path("payload").path("subscription").path("entity")
                        .path("current_end"), subscription.getEndAt()));
            }
            case "subscription.charged" -> {
                subscription.setStatus(SubscriptionStatus.getActive());
                subscription.setLastPaymentAt(LocalDateTime.now());
                subscription.setRazorpayCurrentStart(extractEpochTime(root.path("payload").path("subscription")
                        .path("entity").path("current_start"), subscription.getRazorpayCurrentStart()));
                subscription.setRazorpayCurrentEnd(extractEpochTime(root.path("payload").path("subscription")
                        .path("entity").path("current_end"), subscription.getRazorpayCurrentEnd()));
                subscription.setEndAt(subscription.getRazorpayCurrentEnd());
            }
            case "subscription.halted" -> subscription.setStatus(SubscriptionStatus.getPastDue());
            case "subscription.cancelled" -> subscription.setStatus(SubscriptionStatus.getCanceled());
            case "subscription.completed" -> subscription.setStatus(SubscriptionStatus.getExpired());
            case "payment.captured" -> subscription.setLastPaymentAt(LocalDateTime.now());
            default -> {
                return;
            }
        }

        companySubscriptionRepository.save(subscription);
    }

    private String extractProviderSubscriptionId(JsonNode root) {
        String fromSubscription = root.path("payload").path("subscription").path("entity").path("id")
                .asText(null);

        if (fromSubscription != null && !fromSubscription.isBlank()) {
            return fromSubscription;
        }

        String fromPayment = root.path("payload").path("payment").path("entity").path("subscription_id")
                .asText(null);

        if (fromPayment != null && !fromPayment.isBlank()) {
            return fromPayment;
        }

        return null;
    }

    private LocalDateTime extractEpochTime(JsonNode node, LocalDateTime defaultValue) {
        if (node == null || !node.canConvertToLong()) {
            return defaultValue;
        }

        long epoch = node.asLong();
        if (epoch <= 0) {
            return defaultValue;
        }

        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
