# Subscription + Feature Access Control

## 1) Problem Statement
You want to:
- Sell software plans (subscription billing).
- Allow feature access only if user/company has paid plan.
- Check access on every request for paid/pro feature APIs.
- Let frontend show disabled buttons for unpaid/lower plan features.

This document gives a backend-first design for Spring Boot in this repository.

## 2) Core Design
Use **two layers** of access control:

1. Authentication + Role (already present)
- JWT auth (`JwtAuthenticationFilter`) + role checks in `SecurityConfig`.

2. Subscription + Feature Gate (new)
- For each request, validate if current company has active subscription and required feature.
- If not, return **403 Forbidden** with structured reason/code.

This must be enforced server-side even if frontend hides/disables buttons.

## 3) Data Model (Recommended)
Create these tables/entities.

### 3.1 Plan Definition
`subscription_plans`
- `plan_id` (PK)
- `plan_code` (FREE, STARTER, PRO, ENTERPRISE)
- `name`
- `billing_period` (MONTHLY/YEARLY)
- `price`
- `is_active`

### 3.2 Feature Catalog
`features`
- `feature_id` (PK)
- `feature_code` (e.g. `ORDER_EXPORT`, `OCR_PAYMENT_PROOF`, `ADV_REPORTS`)
- `name`
- `description`
- `is_active`

### 3.3 Plan -> Feature Mapping
`plan_features`
- `plan_feature_id` (PK)
- `plan_id` (FK)
- `feature_id` (FK)
- `limit_value` (nullable; e.g. max exports/month)

### 3.4 Company Subscription (Tenant State)
`company_subscriptions`
- `company_subscription_id` (PK)
- `company_id` (FK to `companies`)
- `plan_id` (FK)
- `status` (`TRIAL`, `ACTIVE`, `PAST_DUE`, `CANCELED`, `EXPIRED`)
- `start_at`
- `end_at`
- `grace_until` (nullable)
- `auto_renew`
- `payment_provider` (set to `RAZORPAY`)
- `provider_subscription_id` (Razorpay subscription id, e.g. `sub_xxx`)
- `last_payment_at`
- `razorpay_customer_id` (nullable; e.g. `cust_xxx`)
- `razorpay_plan_id` (nullable; e.g. `plan_xxx`)
- `razorpay_current_start` (nullable)
- `razorpay_current_end` (nullable)

### 3.5 Billing Events (Audit)
`subscription_invoices` / `subscription_payments`
- Keep invoice/payment history for audit and support.

## 4) Access Control Rules

### 4.1 Subscription Validity
Company is valid for paid features if:
- status = `ACTIVE` OR (`PAST_DUE` and now <= `grace_until`)
- now between `start_at` and `end_at` (or renewable active period)

### 4.2 Feature Access
Feature allowed if:
- feature is mapped to current plan, and
- optional limits are not exceeded.

### 4.3 Hierarchy
- `ENTERPRISE` includes all `PRO` features.
- `PRO` includes `STARTER` features.
- Prefer mapping table over hardcoded hierarchy for flexibility.

## 5) Backend Access Middleware Design

Use annotation + interceptor (or aspect). This is clean and per-endpoint.

### 5.1 Annotation
Create annotation:
- `@RequiresFeature("FEATURE_CODE")`
- Optional: `@RequiresActiveSubscription`

### 5.2 Interceptor/AOP
Before controller method executes:
1. Read current user/company from `SecurityUtil` (`getCurrentCompanyId()`).
2. Load subscription state for company.
3. Validate active/grace.
4. If annotation has feature code, validate plan-feature mapping.
5. If fail -> throw `SubscriptionAccessDeniedException`.

### 5.3 Error Response Contract
Return `403` with machine-readable payload:
- `error`: `FEATURE_NOT_ALLOWED` | `SUBSCRIPTION_INACTIVE` | `PLAN_LIMIT_REACHED`
- `featureCode`
- `requiredPlan` (optional)
- `currentPlan`
- `upgradeUrl` (optional)

This helps frontend disable buttons and show upgrade message.

## 6) API Contracts for Frontend Button Disable

Frontend should not infer from role alone. Provide explicit capability API.

### 6.1 Capability Endpoint
`GET /api/subscription/me/capabilities`

Response example:
```json
{
  "subscription": {
    "planCode": "STARTER",
    "status": "ACTIVE",
    "endAt": "2026-03-01T00:00:00Z"
  },
  "features": {
    "ORDER_EXPORT": false,
    "OCR_PAYMENT_PROOF": true,
    "ADV_REPORTS": false
  },
  "limits": {
    "MONTHLY_EXPORTS": {
      "used": 8,
      "limit": 10,
      "remaining": 2
    }
  }
}
```

### 6.2 Frontend Behavior
- Load capabilities after login and on refresh.
- Disable button if feature flag false.
- Show tooltip/message: "Upgrade to PRO to use this feature".
- Still call backend only for enabled features, but backend remains final authority.

## 7) Suggested Package Structure

Add new module:
- `src/main/java/com/astraval/coreflow/modules/subscription/model/...`
- `src/main/java/com/astraval/coreflow/modules/subscription/repo/...`
- `src/main/java/com/astraval/coreflow/modules/subscription/service/...`
- `src/main/java/com/astraval/coreflow/modules/subscription/controller/...`
- `src/main/java/com/astraval/coreflow/modules/subscription/security/...`

Security-related classes:
- `RequiresFeature.java`
- `RequiresActiveSubscription.java`
- `FeatureAccessInterceptor.java` (or `FeatureAccessAspect.java`)
- `SubscriptionGuardService.java`
- `SubscriptionAccessDeniedException.java`

## 8) Minimal Code Skeleton

### 8.1 Annotation
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresFeature {
    String value();
}
```

### 8.2 Guard Service
```java
public interface SubscriptionGuardService {
    void assertActiveSubscription(Long companyId);
    void assertFeatureAccess(Long companyId, String featureCode);
}
```

### 8.3 Example Usage in Controller
```java
@GetMapping("/reports/advanced")
@RequiresFeature("ADV_REPORTS")
public ApiResponse<ReportDto> getAdvancedReport() {
    // controller logic
}
```

## 9) Integration with Existing Security
- Keep current JWT filter and role checks in `SecurityConfig`.
- Add interceptor/aspect after authentication is available.
- Keep `companyId` claim mandatory in token for tenant-aware checks.
- Fix consistency: `JwtAuthenticationFilter` uses claim `role`, while `SecurityUtil` reads `roleCode`.
  Use one claim key consistently.

## 10) Performance + Caching
- Cache `plan -> feature` mapping (rarely changes).
- Cache company capability snapshot with short TTL (30-120 seconds).
- Invalidate cache when plan/payment webhook updates subscription.

## 11) Payment Webhook Lifecycle
1. Razorpay webhook confirms subscription/payment event.
2. Verify webhook signature (`X-Razorpay-Signature`) before processing.
3. Update `company_subscriptions` (status/end_at/last_payment_at).
4. Store raw webhook payload + event id for audit/idempotency.
5. Invalidate capability cache.
6. Optionally emit domain event for audit/notifications.

Recommended Razorpay events to handle:
- `subscription.activated`
- `subscription.charged`
- `subscription.completed`
- `subscription.cancelled`
- `subscription.halted`
- `payment.captured`

Suggested status mapping:
- `subscription.activated` -> `ACTIVE`
- `subscription.charged` -> keep `ACTIVE`, update period dates
- `subscription.halted` -> `PAST_DUE`
- `subscription.cancelled` -> `CANCELED`
- `subscription.completed` -> `EXPIRED`

Idempotency rule:
- Persist processed `event.id` from Razorpay webhook and ignore duplicates.

## 11.1 Razorpay Integration Notes
- Create plans in Razorpay Dashboard (or API), then map `razorpay_plan_id` to internal `subscription_plans`.
- At checkout, create Razorpay subscription for the company and store `provider_subscription_id`.
- Do not trust client callback alone; subscription state must be finalized only from verified webhook.
- Keep webhook endpoint public but signature-protected (example: `/api/subscription/webhook/razorpay`).

## 12) Testing Strategy
- Unit tests for subscription validity rules.
- Unit tests for feature checks and plan hierarchy.
- Integration tests:
  - Active plan + feature allowed => 200.
  - Inactive subscription => 403 `SUBSCRIPTION_INACTIVE`.
  - Active but missing feature => 403 `FEATURE_NOT_ALLOWED`.
- Webhook tests for subscription state transitions.

## 13) Rollout Plan
1. Create schema + entities + repositories.
2. Add capability endpoint.
3. Implement guard service and annotation checks.
4. Protect only 2-3 premium endpoints first.
5. Add frontend disable logic based on capabilities.
6. Expand to all premium endpoints.

## 14) Important Notes
- UI disable is for UX only; backend check is mandatory security.
- Never trust client flags for billing/feature access.
- Keep audit trail for plan changes and access denials.
