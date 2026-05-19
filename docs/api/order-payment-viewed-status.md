# Order/Payment Viewed (Double Tick) Logic

## Purpose

This document defines how backend sets the `isViewed` flag used by mobile/web to show single-tick vs double-tick in customer/vendor order and payment lists.

## Where It Is Implemented

- `CustomerService`
  - `isViewedOrderStatus(String status)`
  - `isViewedPaymentStatus(String status)`
- `VendorService`
  - `isViewedOrderStatus(String status)`
  - `isViewedPaymentStatus(String status)`

The API response DTOs include `isViewed`:

- Customer: `CustomerOrderSummaryDto`, `CustomerPaymentSummaryDto`
- Vendor: `VendorOrderSummaryDto`, `VendorPaymentSummaryDto`

## Rule

Backend infers viewed state from existing status values (no separate viewed column required).

### Order Viewed (`isViewed = true`)

- `ORDER_VIEWED`
- `ORDER_INVOICED`
- `ORDER_PAYED`

### Payment Viewed (`isViewed = true`)

- `PAYMENT_VIEWED`
- `PAYMENT_ACCEPTED`
- `PAYMENT_DECLINED`
- `PARTIALLY_PAID`
- `PAYMENT_REFUND`
- `PAYMENT_FAILED`

For null/blank or earlier states, `isViewed = false`.

## Why Status-Based Inference

1. Avoids duplicate state storage.
2. Keeps compatibility with existing order/payment workflows.
3. Keeps clients simple (they only read `isViewed` boolean).
