# Subscription APIs

This file lists the newly added APIs for subscription and feature access control.

## 1) Get Current Company Capabilities
- Method: `GET`
- Path: `/api/subscription/me/capabilities`
- Auth: Required (`Bearer <token>`)
- Purpose: Returns current subscription plan/status and feature flags for UI enable/disable logic.

### Success Response (example)
```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Subscription capabilities retrieved successfully",
  "responseData": {
    "subscription": {
      "planCode": "STARTER",
      "status": "ACTIVE",
      "endAt": "2026-03-01T00:00:00"
    },
    "features": {
      "OCR_PAYMENT_PROOF": true,
      "ADV_REPORTS": false
    },
    "limits": {}
  }
}
```

### Error Response
- If company context cannot be resolved:
```json
{
  "responseStatus": false,
  "responseCode": 406,
  "responseMessage": "Unable to resolve current company",
  "responseData": null
}
```

## 2) Razorpay Webhook Receiver
- Method: `POST`
- Path: `/api/subscription/webhook/razorpay`
- Auth: Public endpoint (no JWT), protected by Razorpay signature check.
- Required Header: `X-Razorpay-Signature`
- Body: Raw Razorpay webhook JSON payload.
- Purpose: Updates subscription state from Razorpay events with idempotency.

### Success Response
```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Webhook processed",
  "responseData": "ok"
}
```

### Error Cases
- Missing/invalid signature.
- Webhook secret not configured.
- Invalid payload format.

## 3) Feature-Gated Existing API (updated behavior)
The following existing API is now protected by subscription feature access:
- Method: `POST`
- Path: `/api/companies/{companyId}/payments/payment-proof`
- Required Feature: `OCR_PAYMENT_PROOF`
- Result when not allowed: `403` with subscription access error payload.
