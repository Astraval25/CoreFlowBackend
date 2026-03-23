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

## The Solution: Company-Specific Overlay Tables

Separate the **shared record** (the single-source-of-truth row) from **company-specific metadata** (each company's own view of that record).

### Architecture

```
                    SHARED (single row, both companies read)
                    ┌─────────────────────────────┐
                    │       order_details          │
                    │  order_id = 100              │
                    │  customer = 5                │
                    │  vendor = 8                  │
                    │  total_amount = 50000        │
                    │  paid_amount = 20000         │
                    │  order_status = INVOICED     │
                    │  items, tax, discount...     │
                    └──────────┬──────────────────┘
                               │
              ┌────────────────┴────────────────┐
              │                                 │
    COMPANY-SPECIFIC                  COMPANY-SPECIFIC
    ┌─────────────────────┐          ┌─────────────────────┐
    │ company_order_ref   │          │ company_order_ref   │
    │ company_id = 1      │          │ company_id = 2      │
    │ order_id = 100      │          │ order_id = 100      │
    │ local_order_number  │          │ local_order_number  │
    │   = "INV-032026-5"  │          │   = "PO-032026-12"  │
    │ internal_remarks    │          │ internal_remarks    │
    │   = "Rush order"    │          │   = "Check quality" │
    │ internal_status     │          │ internal_status     │
    │   = "APPROVED"      │          │   = "PENDING_REVIEW"│
    └─────────────────────┘          └─────────────────────┘
       Company A (Seller)               Company B (Buyer)
```

### New Tables

#### `company_order_ref`

Each company gets its own row per order, storing company-specific metadata.

```sql
CREATE TABLE IF NOT EXISTS company_order_ref (
    company_order_ref_id BIGSERIAL PRIMARY KEY,
    company_id           BIGINT NOT NULL REFERENCES companies(comp_id),
    order_id             BIGINT NOT NULL REFERENCES order_details(order_id),
    local_order_number   VARCHAR(50),      -- company's own order number
    internal_remarks     TEXT,              -- private notes (not visible to other party)
    internal_status      VARCHAR(50),       -- company's own approval/workflow status
    internal_tags        VARCHAR(255),      -- company's own categorization
    custom_reference     VARCHAR(100),      -- company's own reference (PO number, etc.)

    -- Audit
    created_by           BIGINT,
    created_dt           TIMESTAMP DEFAULT NOW(),
    modified_by          BIGINT,
    modified_dt          TIMESTAMP,

    UNIQUE(company_id, order_id)
);
```

#### `company_payment_ref`

Same pattern for payments.

```sql
CREATE TABLE IF NOT EXISTS company_payment_ref (
    company_payment_ref_id BIGSERIAL PRIMARY KEY,
    company_id             BIGINT NOT NULL REFERENCES companies(comp_id),
    payment_id             BIGINT NOT NULL REFERENCES payments(payment_id),
    local_payment_number   VARCHAR(50),      -- company's own payment number
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

### How It Works

#### Creating an Order

When Company A (seller) creates a sales order for linked Company B (buyer):

```
1. Insert into order_details          → shared row (order_id = 100)
2. Insert into company_order_ref      → Company A's local ref (INV-032026-5)
3. Insert into company_order_ref      → Company B's local ref (PO-032026-12)
         ↑                                      ↑
   Seller's sequence                      Buyer's sequence
   generate_order_number(companyA)        generate_order_number(companyB)
```

#### Reading an Order

When Company A views the order:
```sql
SELECT o.*, cor.local_order_number, cor.internal_remarks, cor.internal_status
FROM order_details o
JOIN company_order_ref cor ON cor.order_id = o.order_id AND cor.company_id = :myCompanyId
WHERE o.order_id = :orderId
```

Company A sees `local_order_number = INV-032026-5` (their own number).
Company B sees `local_order_number = PO-032026-12` (their own number).
Both see the same `total_amount`, `items`, `paid_amount`, etc.

#### Listing Orders

```sql
-- Company A's sales order list
SELECT o.order_id, cor.local_order_number, o.order_date, o.total_amount, ...
FROM order_details o
JOIN company_order_ref cor ON cor.order_id = o.order_id AND cor.company_id = :myCompanyId
JOIN o.customers c
JOIN c.company sc
WHERE sc.comp_id = :myCompanyId
ORDER BY o.order_date DESC
```

Each company always sees their own order numbers in their own lists.

---

### What Stays in `order_details` vs What Moves to `company_order_ref`

| Column | Stays in `order_details` | Moves to `company_order_ref` | Why |
|---|---|---|---|
| `order_id` | YES | - | Primary key |
| `customer` / `vendor` | YES | - | Defines the relationship |
| `total_amount`, `tax`, `discount` | YES | - | Agreed upon by both parties |
| `paid_amount` | YES | - | Factual, same for both |
| `order_status` | YES | - | Shared transaction status |
| `order_number` | REMOVE | YES (`local_order_number`) | Each company has their own |
| `payment_remarks` | Depends | Could split | Shared vs private remarks |

The `order_number` column on `order_details` can either:
- **Option A**: Be removed entirely (each company uses `company_order_ref.local_order_number`)
- **Option B**: Be kept as a "canonical" reference (the creator's number) while each company also has their own local number

Option B is safer for a gradual migration — keep the existing `order_number` as-is and add the overlay table on top.

---

### For Unlinked Parties

When a company creates an order for an **unlinked** customer/vendor (no linked company on the other side), only ONE `company_order_ref` row is created — for the creating company. There is no second company to generate a number for.

If the parties later become linked, a `company_order_ref` row can be created for the newly linked company at that time.

---

### Summary

| Concept | Table | Visibility |
|---|---|---|
| Transaction data (amount, items, status) | `order_details` / `payments` | Both companies see the SAME data |
| Company-specific metadata (order number, notes) | `company_order_ref` / `company_payment_ref` | Each company sees ONLY THEIR OWN data |

This pattern is called **"shared record + company overlay"** — the shared record is the single source of truth for the transaction, while the overlay tables let each company maintain their own perspective on that transaction without breaking the single-source principle.
