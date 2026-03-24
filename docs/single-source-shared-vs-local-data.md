# Single-Source Tables: Shared vs Company-Specific Data

## The Problem

In CoreFlow, a single `order_details` row represents a transaction between two companies. Both the seller and buyer read from the same row. The same applies to `payments`.

This works well for **shared data** — fields that both parties agree on:

| Shared Data (same for both) |
|---|
| Order items, quantities, prices |
| Total amount, tax, discount |
| Payment amount, date, mode |
| Order/payment status |
| Paid amount |

But some data is **company-specific** — each company needs their own version:

| Company-Specific Data (different per company) |
|---|
| Order number (each company has their own sequence) |
| Payment number (each company has their own sequence) |
| Internal remarks / notes |
| Internal tags or categories |
| Approval status (each company has their own approval workflow) |
| Custom reference numbers |

### The Conflict

Today, `order_details` has ONE `order_number` column. When Company A (seller) creates a sales order, it generates `ORD-032026-5` from Company A's sequence. Company B (buyer) sees this same order — but with Company A's order number, not their own.

```
Company A (Seller)                    Company B (Buyer)
Creates sales order                   Sees it as purchase order
order_number = ORD-032026-5           order_number = ORD-032026-5  <-- This is A's number, not B's!
```

In real business, both companies maintain their own reference systems:

```
Company A calls it:  INV-032026-5     (their 5th invoice this month)
Company B calls it:  PO-032026-12     (their 12th purchase order this month)
Same transaction. Different reference numbers.
```

The same problem exists for `payments`:

```
Company A (Buyer/Sender)              Company B (Seller/Receiver)
Creates payment                       Sees the payment
payment_number = PAY-032026-3         payment_number = PAY-032026-3  <-- A's number, not B's!
```

---

## The Solution: Platform Reference + Company-Specific Overlay

Two layers solve this problem:

1. **Platform Reference Number** — a globally unique identifier on the shared row that both parties use to refer to the same transaction (like a tracking number).
2. **Company Overlay Table** — each company gets their own row with their own local number, remarks, and metadata.

### Architecture

```
                        SHARED (single row, both companies read)
                        ┌─────────────────────────────────────┐
                        │          order_details               │
                        │  order_id = 100                      │
                        │  platform_ref = "CF-ORD-20260324-100"│  <-- common identifier
                        │  customer = 5                        │
                        │  vendor = 8                          │
                        │  total_amount = 50000                │
                        │  paid_amount = 20000                 │
                        │  order_status = INVOICED             │
                        │  items, tax, discount...             │
                        └──────────────┬──────────────────────┘
                                       │
                  ┌────────────────────┴─────────────────────┐
                  │                                          │
       COMPANY-SPECIFIC                           COMPANY-SPECIFIC
       ┌──────────────────────┐                   ┌──────────────────────┐
       │  company_order_ref   │                   │  company_order_ref   │
       │  company_id = 1      │                   │  company_id = 2      │
       │  order_id = 100      │                   │  order_id = 100      │
       │  local_order_number  │                   │  local_order_number  │
       │    = "SO-032026-5"   │                   │    = "PO-032026-12"  │
       │  internal_remarks    │                   │  internal_remarks    │
       │    = "Rush order"    │                   │    = "Check quality" │
       │  internal_status     │                   │  internal_status     │
       │    = "APPROVED"      │                   │    = "PENDING_REVIEW"│
       └──────────────────────┘                   └──────────────────────┘
          Company A (Seller)                         Company B (Buyer)
```

---

## Platform Reference Number

A **platform-level identifier** that uniquely identifies an order or payment across the entire platform. Both companies see and can share this number to refer to the same transaction — like a tracking number.

### Format

```
CF-ORD-YYYYMMDD-{SEQ}      (orders)
CF-PAY-YYYYMMDD-{SEQ}      (payments)
```

Examples:
- `CF-ORD-20260324-1042` — the 1042nd order created on the platform on 2026-03-24
- `CF-PAY-20260324-387` — the 387th payment on that date

### Where it lives

```sql
-- On the shared order_details table
ALTER TABLE order_details ADD COLUMN platform_ref VARCHAR(50) UNIQUE;

-- On the shared payments table
ALTER TABLE payments ADD COLUMN platform_ref VARCHAR(50) UNIQUE;
```

### Generation (platform-wide sequence, NOT per-company)

```sql
CREATE SEQUENCE IF NOT EXISTS platform_order_seq;
CREATE SEQUENCE IF NOT EXISTS platform_payment_seq;

CREATE OR REPLACE FUNCTION generate_platform_order_ref()
RETURNS TEXT AS $$
BEGIN
  RETURN 'CF-ORD-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || nextval('platform_order_seq');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_platform_payment_ref()
RETURNS TEXT AS $$
BEGIN
  RETURN 'CF-PAY-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || nextval('platform_payment_seq');
END;
$$ LANGUAGE plpgsql;
```

### Use cases for Platform Reference

- **Cross-company communication**: "I'm calling about order CF-ORD-20260324-1042" — both parties know exactly which order.
- **Support tickets**: Customer support can look up any transaction by platform ref.
- **Payment matching**: Link a payment to an order using platform refs visible to both parties.
- **Audit trail**: A single reference that never changes regardless of each company's local numbering.

---

## Company-Specific Overlay Tables

### `company_order_ref`

Each company gets its own row per order, storing company-specific metadata.

```sql
CREATE TABLE IF NOT EXISTS company_order_ref (
    company_order_ref_id BIGSERIAL PRIMARY KEY,
    company_id           BIGINT NOT NULL REFERENCES companies(comp_id),
    order_id             BIGINT NOT NULL REFERENCES order_details(order_id),
    local_order_number   VARCHAR(50),      -- company's own order number (customizable format)
    internal_remarks     TEXT,              -- private notes (not visible to other party)
    internal_status      VARCHAR(50),       -- company's own approval/workflow status
    internal_tags        VARCHAR(255),      -- company's own categorization
    custom_reference     VARCHAR(100),      -- company's own external reference (PO number, etc.)

    -- Audit
    created_by           BIGINT,
    created_dt           TIMESTAMP DEFAULT NOW(),
    modified_by          BIGINT,
    modified_dt          TIMESTAMP,

    UNIQUE(company_id, order_id)
);
```

### `company_payment_ref`

Same pattern for payments.

```sql
CREATE TABLE IF NOT EXISTS company_payment_ref (
    company_payment_ref_id BIGSERIAL PRIMARY KEY,
    company_id             BIGINT NOT NULL REFERENCES companies(comp_id),
    payment_id             BIGINT NOT NULL REFERENCES payments(payment_id),
    local_payment_number   VARCHAR(50),      -- company's own payment number (customizable format)
    internal_remarks       TEXT,
    internal_status        VARCHAR(50),
    custom_reference       VARCHAR(100),

    -- Audit
    created_by             BIGINT,
    created_dt             TIMESTAMP DEFAULT NOW(),
    modified_by            BIGINT,
    modified_dt            TIMESTAMP,

    UNIQUE(company_id, payment_id)
);
```

### Company-Level Number Format (Customizable)

Each company configures their own number format via the `company_config` table (see [customization.md](customization.md)).

```
Company A (Seller) config:
  sales_order_prefix  = "SO"
  format              = "{PREFIX}-{MMYYYY}-{SEQ}"
  Result              = "SO-032026-5"

Company B (Buyer) config:
  purchase_order_prefix = "PO"
  format                = "{PREFIX}-{MMYYYY}-{SEQ}"
  Result                = "PO-032026-12"
```

The `generate_order_number()` function reads the company's format config and generates accordingly. See [customization.md](customization.md) for the full format specification.

---

## How It Works End-to-End

### Creating a Sales Order (linked parties)

When Company A (seller) creates a sales order for linked Company B (buyer):

```
Step 1: Insert into order_details
        → platform_ref = generate_platform_order_ref()     = "CF-ORD-20260324-1042"
        → customer = 5, vendor = 8
        → total_amount, tax, items...

Step 2: Insert into company_order_ref for Company A (seller)
        → local_order_number = generate_order_number(companyA)  = "SO-032026-5"
           (uses Company A's sales order format config)

Step 3: Insert into company_order_ref for Company B (buyer)
        → local_order_number = generate_order_number(companyB)  = "PO-032026-12"
           (uses Company B's purchase order format config)
```

### Creating an Order (unlinked party)

When a company creates an order for an **unlinked** customer/vendor, only ONE `company_order_ref` row is created — for the creating company. The platform ref is still generated on the shared row.

If the parties later become linked, a `company_order_ref` row can be created for the newly linked company at that time.

### Reading an Order

When Company A views the order:
```sql
SELECT o.order_id, o.platform_ref, o.total_amount, o.order_status,
       cor.local_order_number, cor.internal_remarks, cor.internal_status
FROM order_details o
JOIN company_order_ref cor ON cor.order_id = o.order_id AND cor.company_id = :myCompanyId
WHERE o.order_id = :orderId
```

| Field | Company A sees | Company B sees |
|---|---|---|
| `platform_ref` | CF-ORD-20260324-1042 | CF-ORD-20260324-1042 (same) |
| `local_order_number` | SO-032026-5 | PO-032026-12 |
| `total_amount` | 50000 | 50000 (same) |
| `internal_remarks` | "Rush order" | "Check quality" |

### Creating a Payment (linked parties)

Same pattern:

```
Step 1: Insert into payments
        → platform_ref = generate_platform_payment_ref()   = "CF-PAY-20260324-387"
        → customer = 5, vendor = 8, amount, mode...

Step 2: Insert into company_payment_ref for sender (buyer)
        → local_payment_number = generate_payment_number(buyerCompany) = "PAY-032026-3"

Step 3: Insert into company_payment_ref for receiver (seller)
        → local_payment_number = generate_payment_number(sellerCompany) = "REC-032026-8"
```

---

## What Stays in `order_details` vs What Goes Where

| Data | Table | Why |
|---|---|---|
| `order_id` | `order_details` | Primary key |
| `platform_ref` | `order_details` | Common identifier for both parties |
| `customer` / `vendor` | `order_details` | Defines the B2B relationship |
| `total_amount`, `tax`, `discount` | `order_details` | Agreed upon by both parties |
| `paid_amount` | `order_details` | Factual, same for both |
| `order_status` | `order_details` | Shared transaction status |
| `order_number` | `company_order_ref` (`local_order_number`) | Each company has their own |
| Private remarks | `company_order_ref` (`internal_remarks`) | Not visible to other party |
| Internal approval | `company_order_ref` (`internal_status`) | Each company's own workflow |

### Migration approach

The existing `order_number` column on `order_details` can be kept as-is during migration. The overlay tables are additive — no existing data or API needs to break. Gradually:
1. Add `platform_ref` to shared tables
2. Add overlay tables
3. Populate overlay `local_order_number` from existing `order_number` for creator company
4. Update APIs to read from overlay instead of shared `order_number`
5. Eventually deprecate the shared `order_number` column

---

## Three-Layer Summary

| Layer | Table | Scope | Example |
|---|---|---|---|
| Platform identity | `order_details.platform_ref` | Global, same for everyone | `CF-ORD-20260324-1042` |
| Shared transaction data | `order_details.*` | Both companies see the same values | `total_amount = 50000` |
| Company-specific metadata | `company_order_ref` | Each company sees only their own | `local_order_number = "SO-032026-5"` |

This design ensures:
- Both parties can always identify the same transaction (platform ref)
- Transaction data is never duplicated (single-source)
- Each company maintains full control over their own numbering, notes, and workflows (overlay)
