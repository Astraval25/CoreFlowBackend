# Response Changes

New fields added to existing order and payment API responses.

---

## New Fields in Order Responses

### `platformRef` (String, nullable)

A platform-wide unique identifier for the order, visible to both buyer and seller. Format: `CF-ORD-YYYYMMDD-SEQ` (e.g., `CF-ORD-20260324-1`).

Added to:

| Endpoint | DTO | Notes |
|----------|-----|-------|
| `GET /api/companies/{id}/sales/orders` | `SalesOrderSummaryDto` | New field at end of record |
| `GET /api/companies/{id}/purchase/orders` | `PurchaseOrderSummaryDto` | New field at end of record |
| `GET /api/companies/{id}/orders/{orderId}` | `OrderDetailsFullResponse` | New field after `orderNumber` |
| `GET /api/companies/{id}/sales/orders/history` | Snapshot `SalesOrderSummaryDto` | New field at end of record |
| `GET /api/companies/{id}/purchase/orders/history` | Snapshot `PurchaseOrderSummaryDto` | New field at end of record |

### Example: Sales Order Summary (before)

```json
{
  "orderId": 1,
  "orderNumber": "ORD-032026-1",
  "orderDate": "2026-03-24T10:00:00",
  "buyerCompanyName": "Acme Corp",
  "vendorName": "Acme Corp",
  "totalAmount": 1500.00,
  "paidAmount": 0.00,
  "orderStatus": "ORDER_VIEWED",
  "isActive": true
}
```

### Example: Sales Order Summary (after)

```json
{
  "orderId": 1,
  "orderNumber": "ORD-032026-1",
  "orderDate": "2026-03-24T10:00:00",
  "buyerCompanyName": "Acme Corp",
  "vendorName": "Acme Corp",
  "totalAmount": 1500.00,
  "paidAmount": 0.00,
  "orderStatus": "ORDER_VIEWED",
  "isActive": true,
  "platformRef": "CF-ORD-20260324-1"
}
```

### Example: Order Full Detail (new fields)

```json
{
  "orderId": 1,
  "orderNumber": "ORD-032026-1",
  "platformRef": "CF-ORD-20260324-1",
  "orderDate": "2026-03-24T10:00:00",
  ...
}
```

---

## New Fields in Payment Responses

### `platformRef` (String, nullable)

A platform-wide unique identifier for the payment. Format: `CF-PAY-YYYYMMDD-SEQ` (e.g., `CF-PAY-20260324-1`).

Added to:

| Endpoint | DTO | Notes |
|----------|-----|-------|
| `GET /api/companies/{id}/payments/made` | `PayerPaymentSummaryDto` | New field after `referenceNumber` |
| `GET /api/companies/{id}/payments/received` | `SellerPaymentSummaryDto` | New field after `referenceNumber` |
| `GET /api/companies/{id}/payments/{paymentId}` | `PaymentViewDto` | New field after `paymentNumber` |

### Example: Payment Summary (after)

```json
{
  "paymentId": 1,
  "paymentDate": "2026-03-24T10:00:00",
  "orderIds": "1, 2",
  "paymentNumber": "PAY-032026-1",
  "amount": 500.00,
  "vendorName": "Supplier Inc",
  "modeOfPayment": "BANK_TRANSFER",
  "paymentStatus": "PAID",
  "isActive": true,
  "referenceNumber": "TXN-12345",
  "platformRef": "CF-PAY-20260324-1"
}
```

---

## Backward Compatibility

- All new fields are **additive** and **nullable**
- Existing fields (`orderNumber`, `paymentNumber`) remain unchanged
- Existing orders/payments created before this update will have `platformRef = null`
- No request body changes for existing endpoints

---

## New API Endpoints

Two new API groups have been added:

1. **[Config API](config-api.md)** - Company-specific configuration overrides (number formats, prefixes)
2. **[Company Ref API](company-ref-api.md)** - Per-company overlay data for orders and payments (local numbers, internal remarks, status, tags)
