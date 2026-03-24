# Customer/Vendor Specific Item Pricing

## Overview

In CoreFlow, items (products/services) belong to a company. Each item has **base prices** set by the owning company. However, the price charged to a specific customer or paid to a specific vendor can differ from the base price.

This is handled by two overlay tables:
- **`item_customer_price`** — per-customer sales price override
- **`item_vendor_price`** — per-vendor purchase price override

```
                    ┌─────────────────────────┐
                    │         Items            │
                    │  item_id = 10            │
                    │  company = 1             │
                    │  item_name = "Widget"    │
                    │  base_sales_price = 100  │
                    │  base_purchase_price = 60│
                    │  sales_description =     │
                    │    "Standard widget"     │
                    └────────────┬─────────────┘
                                 │
            ┌────────────────────┴────────────────────┐
            │                                         │
  ┌─────────────────────────┐            ┌────────────────────────────┐
  │  item_customer_price    │            │  item_vendor_price         │
  │  item = 10              │            │  item = 10                 │
  │  customer = 5           │            │  vendor = 8                │
  │  sales_price = 90       │            │  purchase_price = 55       │
  │  sales_description =    │            │  purchase_description =    │
  │    "Discounted for VIP" │            │    "Bulk rate from ABC"    │
  └─────────────────────────┘            └────────────────────────────┘
     Customer 5 gets ₹90                    Vendor 8 charges ₹55
     (instead of base ₹100)                 (instead of base ₹60)
```

---

## How It Works

### Price Resolution (Cascading Priority)

When the system needs a price for an item, it follows this priority:

```
1. Customer/Vendor specific price  →  if set, use it
2. Base item price                 →  fallback
```

This logic is in `CustomerItemService.resolveSalesPrice()` and `VendorItemService.resolvePurchasePrice()`:

```java
// CustomerItemService
private BigDecimal resolveSalesPrice(Items item, ItemCustomerPrice price) {
    if (price != null && price.getSalesPrice() != null) {
        return price.getSalesPrice();       // customer-specific price wins
    }
    return item.getBaseSalesPrice();        // fallback to base
}

// VendorItemService
private BigDecimal resolvePurchasePrice(Items item, ItemVendorPrice price) {
    if (price != null && price.getPurchasePrice() != null) {
        return price.getPurchasePrice();    // vendor-specific price wins
    }
    return item.getBasePurchasePrice();     // fallback to base
}
```

The same cascade applies to descriptions (`salesDescription` / `purchaseDescription`).

### Source Tracking

Each item returned to the frontend includes a `source` field indicating where the price came from:

| Source | Meaning |
|---|---|
| `CUSTOMER_ITEM` / `VENDOR_ITEM` | Price or description is overridden for this customer/vendor |
| `ITEM_BASE` | Using the base item price (no override exists) |

This helps the UI distinguish between custom-priced and default-priced items.

---

## Customer Item Service (`CustomerItemService`)

Manages per-customer sales price overrides. The company (seller) sets special prices for specific customers.

### Operations

| Operation | Method | What it does |
|---|---|---|
| **Create** | `createCustomerItem(companyId, customerId, dto)` | Sets a customer-specific sales price for an item. If a deactivated mapping exists, reactivates it with new values. |
| **Update** | `updateCustomerItem(companyId, customerId, itemId, dto)` | Updates the sales price and/or description for an existing mapping. |
| **Deactivate** | `deactivateCustomerItem(companyId, customerId, itemId)` | Soft-deletes the mapping (`isActive = false`). The base price will be used instead. |
| **Activate** | `activateCustomerItem(companyId, customerId, itemId)` | Re-enables a previously deactivated mapping. |
| **List all items** | `getItemsByCustomer(companyId, customerId)` | Returns ALL company items with resolved prices for this customer (base or override). |
| **List active items** | `getActiveItemsByCustomer(companyId, customerId)` | Same, but only active items. |
| **List mapped items** | `getMappedItemsByCustomer(companyId, customerId)` | Returns ONLY items that have a customer-specific price set. |
| **Get item detail** | `getItemDetail(companyId, customerId, itemId)` | Returns full item detail with resolved price for this customer. |

### Create Flow

```
1. Validate customer belongs to company
2. Validate item belongs to company
3. Check if mapping already exists:
   a. If exists AND active → throw error (duplicate)
   b. If exists AND inactive → reactivate with new values
   c. If not exists → create new ItemCustomerPrice
4. Save and return ID
```

### Active Status Logic

An item is considered "active" for a customer when BOTH conditions are true:
- The item itself is active (`item.isActive = true`)
- The customer price mapping is active (`itemCustomerPrice.isActive = true`), OR no mapping exists (falls back to base)

```java
boolean itemActive = Boolean.TRUE.equals(item.getIsActive());
boolean mappingActive = !isCustomerItemSource || Boolean.TRUE.equals(price.getIsActive());
Boolean isActive = itemActive && mappingActive;
```

---

## Vendor Item Service (`VendorItemService`)

Manages per-vendor purchase price overrides. The company (buyer) records what each vendor charges them.

### Operations

| Operation | Method | What it does |
|---|---|---|
| **Create** | `createVendorItem(companyId, vendorId, dto)` | Sets a vendor-specific purchase price for an item. |
| **Update** | `updateVendorItem(companyId, vendorId, itemId, dto)` | Updates the purchase price and/or description. |
| **Deactivate** | `deactivateVendorItem(companyId, vendorId, itemId)` | Soft-deletes the mapping. |
| **Activate** | `activateVendorItem(companyId, vendorId, itemId)` | Re-enables a deactivated mapping. |
| **List all items** | `getItemsByVendor(companyId, vendorId)` | Returns ALL company items with resolved prices for this vendor. |
| **List active items** | `getActiveItemsByVendor(companyId, vendorId)` | Same, but only active items. |
| **List mapped items** | `getMappedItemsByVendor(companyId, vendorId)` | Returns ONLY items that have a vendor-specific price set. |
| **Get item detail** | `getItemDetail(companyId, vendorId, itemId)` | Returns full item detail with resolved price for this vendor. |

---

## Use Cases in the Application

### Use Case 1: Order Line Item Pricing

When creating a sales order for Customer X, the order form shows items with Customer X's prices (if set) or base prices (if not). This ensures the correct price appears on the invoice automatically.

```
Creating sales order for Customer X:
  Item: Widget     → Customer X price: ₹90 (override)
  Item: Gadget     → Base price: ₹200 (no override)
  Item: Service A  → Customer X price: ₹500 (override)
```

### Use Case 2: Purchase Order Pricing

When creating a purchase order from Vendor Y, the company sees Vendor Y's specific prices — what that vendor charges them.

```
Creating purchase order for Vendor Y:
  Item: Raw Material A  → Vendor Y price: ₹55 (override)
  Item: Raw Material B  → Base price: ₹80 (no override)
```

### Use Case 3: Price Negotiation

A company negotiates a special rate with a specific customer. Instead of changing the base price (which affects all customers), they set a customer-specific price:

```
Base sales price: ₹100

Customer A (regular):     ₹100 (base, no override)
Customer B (wholesale):   ₹85  (customer-specific override)
Customer C (VIP):         ₹75  (customer-specific override)
```

### Use Case 4: Linked Company Item Visibility

When two companies are linked (via `CustomerVendorLink`), the buyer company can see the seller's items through the vendor item service. The seller sets customer-specific prices, and the buyer's purchase order reflects those prices.

```
Company A (Seller)                    Company B (Buyer)
┌─────────────────────────┐           ┌─────────────────────────┐
│ Items:                  │           │ Vendors:                │
│   Widget (base: ₹100)  │           │   Company A             │
│                         │           │                         │
│ Customers:              │           │ Vendor Items:           │
│   Company B             │           │   Widget → ₹90          │
│                         │           │   (from Company A's     │
│ Customer Items:         │           │    customer price for   │
│   Company B → ₹90      │           │    Company B)           │
└─────────────────────────┘           └─────────────────────────┘
```

### Use Case 5: Deactivation without Deletion

When a customer no longer gets a special price, the mapping is deactivated (not deleted). This preserves history and allows reactivation later:

```
Customer B's special price: ₹85 (active)
  → Deactivate: Customer B now sees base price ₹100
  → Reactivate later: Customer B sees ₹85 again (same mapping, preserved values)
```

---

## Data Model

### `ItemCustomerPrice`

| Column | Type | Description |
|---|---|---|
| `item_customer_price_id` | Long (PK) | Primary key |
| `item` | FK → Items | The item being priced |
| `customer` | FK → Customers | The customer this price applies to |
| `sales_price` | BigDecimal | Customer-specific sales price (nullable — null means use base) |
| `sales_description` | String | Customer-specific description (nullable) |
| `is_active` | Boolean | Soft-delete flag |
| Audit fields | | `created_by`, `created_dt`, `modified_by`, `modified_dt` |

### `ItemVendorPrice`

| Column | Type | Description |
|---|---|---|
| `item_vendor_price_id` | Long (PK) | Primary key |
| `item` | FK → Items | The item being priced |
| `vendor` | FK → Vendors | The vendor this price applies to |
| `purchase_price` | BigDecimal | Vendor-specific purchase price (nullable) |
| `purchase_description` | String | Vendor-specific description (nullable) |
| `is_active` | Boolean | Soft-delete flag |
| Audit fields | | `created_by`, `created_dt`, `modified_by`, `modified_dt` |

### Key Constraints

- One price mapping per (item, customer) pair — enforced by unique lookup in service code
- One price mapping per (item, vendor) pair — same enforcement
- Both item and customer/vendor must belong to the same company — validated in every operation
