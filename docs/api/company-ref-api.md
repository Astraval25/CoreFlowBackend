# Company Ref API

Per-company overlay data for orders and payments. Each company in a B2B transaction gets its own overlay row with a local number, internal remarks, status, tags, and custom reference.

## Base URL

```
/api/companies/{companyId}
```

---

## Order Ref

### 1. Get Order Ref

Returns the company's overlay data for a specific order.

```
GET /api/companies/{companyId}/orders/{orderId}/ref
```

#### Response

```json
{
  "status": 200,
  "message": "Order ref retrieved",
  "data": {
    "companyOrderRefId": 1,
    "localOrderNumber": "SO-032026-1",
    "internalRemarks": null,
    "internalStatus": null,
    "internalTags": null,
    "customReference": null
  }
}
```

### 2. Update Order Ref

Updates the company's internal metadata for an order. Only provided fields are updated (null fields are skipped).

```
PUT /api/companies/{companyId}/orders/{orderId}/ref
```

#### Request Body

```json
{
  "internalRemarks": "Priority shipment - customer requested express",
  "internalStatus": "IN_REVIEW",
  "internalTags": "express,priority",
  "customReference": "PO-2026-0042"
}
```

#### Response

```json
{
  "status": 200,
  "message": "Order ref updated",
  "data": "Updated"
}
```

---

## Payment Ref

### 3. Get Payment Ref

Returns the company's overlay data for a specific payment.

```
GET /api/companies/{companyId}/payments/{paymentId}/ref
```

#### Response

```json
{
  "status": 200,
  "message": "Payment ref retrieved",
  "data": {
    "companyPaymentRefId": 1,
    "localPaymentNumber": "PAY-032026-1",
    "internalRemarks": null,
    "internalStatus": null,
    "customReference": null
  }
}
```

### 4. Update Payment Ref

Updates the company's internal metadata for a payment.

```
PUT /api/companies/{companyId}/payments/{paymentId}/ref
```

#### Request Body

```json
{
  "internalRemarks": "Received via bank transfer",
  "internalStatus": "VERIFIED",
  "customReference": "TXN-98765"
}
```

#### Response

```json
{
  "status": 200,
  "message": "Payment ref updated",
  "data": "Updated"
}
```

---

## Field Reference

### Order Ref Fields

| Field | Type | Description |
|-------|------|-------------|
| `companyOrderRefId` | Long | Primary key |
| `localOrderNumber` | String | Company-specific order number (auto-generated on creation) |
| `internalRemarks` | String | Free-text internal notes |
| `internalStatus` | String | Company-private status label |
| `internalTags` | String | Comma-separated tags |
| `customReference` | String | Custom reference (e.g., counterparty's PO number) |

### Payment Ref Fields

| Field | Type | Description |
|-------|------|-------------|
| `companyPaymentRefId` | Long | Primary key |
| `localPaymentNumber` | String | Company-specific payment number (auto-generated on creation) |
| `internalRemarks` | String | Free-text internal notes |
| `internalStatus` | String | Company-private status label |
| `customReference` | String | Custom reference (e.g., bank transaction ID) |

---

## How Overlays Are Created

Overlay rows are **automatically created** when an order or payment is created:

- **Sales order**: Seller gets an overlay with `SALES_ORDER` local number. If buyer is linked, buyer gets an overlay with `PURCHASE_ORDER` local number.
- **Purchase order**: Buyer gets `PURCHASE_ORDER` local number. If seller is linked, seller gets `SALES_ORDER` local number.
- **Buyer payment**: Buyer gets `PAYMENT_OUT` local number. If seller is linked, seller gets `PAYMENT_IN` local number.
- **Seller payment**: Seller gets `PAYMENT_IN` local number. If buyer is linked, buyer gets `PAYMENT_OUT` local number.

The local number format is determined by the company's config (see [Config API](config-api.md)).
