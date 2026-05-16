# Marketplace + Expense + Company Profile API Updates

This document captures the newly added and updated APIs for mobile app integration.

## What Was Updated

### 1) New module: Expense Account (public schema)
- Added table: `public.expense_accounts`
- Purpose: company-level configurable expense ledger accounts
- Account type is restricted to allowed values
- Account name must be unique per company
- On new company creation, default expense accounts are auto-seeded to reduce first-time setup friction

### 2) New module: Expense (public schema)
- Added table: `public.expenses`
- Purpose: record outgoing/incoming expense entries
- Supports positive and negative `amount`
- Mandatory fields: `expenseDate`, `paymentMode`, `amount`, `expenseAccountId`
- Optional fields: `invoiceNo`, `vendorId`, `customerId`, `remark`
- Optional `salaryPeriodId` links an expense to a salary period for salary payment tracking
- Salary expenses are now created only when admin records payment from the Salary screen

### 3) New module: Marketplace
- Public listing endpoints to explore companies and what they sell
- Returns only active, sellable items with `salesPrice` (no purchase-side item listing)

### 4) Updated Company profile APIs
- Company create/update/detail now includes richer profile fields:
  - `contactPerson`, `contactEmail`, `contactPhone`, `website`
  - `addressLine1`, `addressLine2`, `city`, `state`, `country`, `postalCode`
  - `publicDescription`
- Supports organization profile use-case in settings

## Security / Access

- `/api/companies/**` -> `ROLE_ADM` or `ROLE_USR`
- `GET /api/marketplace/**` -> `permitAll` (public, no auth required)
- Non-GET marketplace operations are not exposed currently

All APIs use bearer token auth unless explicitly public in auth module.

## Common Response Wrapper

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Message",
  "responseData": {}
}
```

Error shape:

```json
{
  "responseStatus": false,
  "responseCode": 406,
  "responseMessage": "Error message",
  "responseData": null
}
```

## Response Code Mapping
- `201` created
- `202` accepted / fetched
- `203` updated
- `204` deleted
- `406` business/runtime validation error

---

## Expense Account APIs

Base: `/api/companies/{companyId}/expense-accounts`

### Allowed Account Types
Use this API to fetch supported values:

- `GET /api/companies/{companyId}/expense-accounts/account-types`

Current values:
- Asset
- Other Asset
- Other Current Asset
- Fixed Asset
- Intangible Asset
- Non Current Asset
- Liability
- Other Current Liability
- Non Current Liability
- Other Liability
- Expense
- Cost Of Goods Sold
- Other Expense

### 1) Create Expense Account
- `POST /api/companies/{companyId}/expense-accounts`

Request:
```json
{
  "accountType": "Expense",
  "accountName": "Travel Expense"
}
```

Success:
```json
{
  "responseStatus": true,
  "responseCode": 201,
  "responseMessage": "Expense account created successfully",
  "responseData": {
    "expenseAccountId": 12
  }
}
```

### 2) List Expense Accounts
- `GET /api/companies/{companyId}/expense-accounts?activeOnly={true|false}`

### 3) Get Expense Account Detail
- `GET /api/companies/{companyId}/expense-accounts/{expenseAccountId}`

### 4) Update Expense Account
- `PUT /api/companies/{companyId}/expense-accounts/{expenseAccountId}`

Request:
```json
{
  "accountType": "Expense",
  "accountName": "Fuel Expense"
}
```

### 5) Deactivate Expense Account
- `PATCH /api/companies/{companyId}/expense-accounts/{expenseAccountId}/deactivate`

### 6) Activate Expense Account
- `PATCH /api/companies/{companyId}/expense-accounts/{expenseAccountId}/activate`

ExpenseAccount response object:
```json
{
  "expenseAccountId": 12,
  "accountType": "Expense",
  "accountName": "Fuel Expense",
  "isActive": true,
  "createdDt": "2026-05-10T14:12:00",
  "lastModifiedDt": "2026-05-10T14:12:00"
}
```

Common error messages:
- `Company not found with ID: {companyId}`
- `Invalid account type. Valid account types are: ...`
- `Account type is required`
- `Account name is required`
- `Expense account '{accountName}' already exists for this company`
- `Expense account not found with ID: {expenseAccountId}`

---

## Expense APIs

Base: `/api/companies/{companyId}/expenses`

### 1) Create Expense
- `POST /api/companies/{companyId}/expenses`

Request:
```json
{
  "expenseDate": "2026-05-11",
  "paymentMode": "UPI",
  "amount": -550.00,
  "expenseAccountId": 12,
  "invoiceNo": "INV-2026-44",
  "vendorId": 4,
  "customerId": null,
  "remark": "Fuel refill",
  "salaryPeriodId": null
}
```

Success:
```json
{
  "responseStatus": true,
  "responseCode": 201,
  "responseMessage": "Expense created successfully",
  "responseData": {
    "expenseId": 101
  }
}
```

### 2) List Expenses
- `GET /api/companies/{companyId}/expenses?activeOnly={true|false}`

### 3) Get Expense Detail
- `GET /api/companies/{companyId}/expenses/{expenseId}`

### 4) Update Expense
- `PUT /api/companies/{companyId}/expenses/{expenseId}`

### 5) Deactivate Expense
- `PATCH /api/companies/{companyId}/expenses/{expenseId}/deactivate`

### 6) Activate Expense
- `PATCH /api/companies/{companyId}/expenses/{expenseId}/activate`

Expense response object:
```json
{
  "expenseId": 101,
  "expenseDate": "2026-05-11",
  "paymentMode": "UPI",
  "amount": -550.0,
  "expenseAccountId": 12,
  "expenseAccountName": "Fuel Expense",
  "expenseAccountType": "Expense",
  "invoiceNo": "INV-2026-44",
  "vendorId": 4,
  "vendorName": "ABC Fuels",
  "customerId": null,
  "customerName": null,
  "remark": "Fuel refill",
  "salaryPeriodId": 88,
  "isActive": true,
  "createdDt": "2026-05-11T10:11:00",
  "lastModifiedDt": "2026-05-11T10:11:00"
}
```

Common error messages:
- `Company not found with ID: {companyId}`
- `Expense not found with ID: {expenseId}`
- `Expense account not found with ID: {expenseAccountId}`
- `Expense account '{accountName}' is inactive`
- `Vendor not found with ID: {vendorId}`
- `Customer not found with ID: {customerId}`
- `Expense date is required`
- `Payment mode is required`
- `Amount is required`
- `Expense account is required`
- `Salary payment amount must be greater than zero`
- `Salary period not found with ID: {salaryPeriodId}`
- `Salary period must be approved before recording payment`

---

## Marketplace APIs

Base: `/api/marketplace`

### Behavior
- Company list includes active companies.
- Item list includes only sellable product-facing data (sales side), not purchase-side pricing.

### 1) List Marketplace Companies
- `GET /api/marketplace/companies`

Company object:
```json
{
  "companyId": 1,
  "companyName": "Astraval Trading",
  "industry": "Textile",
  "shortName": "AT",
  "fsId": "file-id-logo",
  "contactPerson": "Arun",
  "contactEmail": "sales@astraval.com",
  "contactPhone": "9999999999",
  "website": "https://astraval.com",
  "addressLine1": "No. 10 Main Road",
  "addressLine2": "Industrial Area",
  "city": "Coimbatore",
  "state": "Tamil Nadu",
  "country": "India",
  "postalCode": "641001",
  "publicDescription": "Manufacturer and supplier of textile components."
}
```

### 2) Get Marketplace Company Detail
- `GET /api/marketplace/companies/{companyId}`

### 3) Get Marketplace Company Sellable Items
- `GET /api/marketplace/companies/{companyId}/items`

Item object:
```json
{
  "itemId": 55,
  "itemName": "Cotton Yarn 40s",
  "itemType": "PRODUCT",
  "unit": "KG",
  "salesDescription": "Premium combed cotton yarn",
  "salesPrice": 310.0,
  "taxRate": 5.0,
  "hsnCode": "5205",
  "fsId": "file-id-item-image"
}
```

Common error messages:
- `Company not found with ID: {companyId}`
- `Company is inactive`

---

## Updated Company APIs (Organization Profile ready)

Base: `/api/companies`

Updated endpoints:
- `POST /api/companies`
- `PUT /api/companies/{companyId}`
- `GET /api/companies/{companyId}`

### Updated create/update request fields
```json
{
  "companyName": "Astraval Trading",
  "industry": "Textile",
  "pan": "ABCDE1234F",
  "gstNo": "33ABCDE1234F1Z5",
  "hsnCode": "5205",
  "shortName": "AT",
  "contactPerson": "Arun",
  "contactEmail": "sales@astraval.com",
  "contactPhone": "9999999999",
  "website": "https://astraval.com",
  "addressLine1": "No. 10 Main Road",
  "addressLine2": "Industrial Area",
  "city": "Coimbatore",
  "state": "Tamil Nadu",
  "country": "India",
  "postalCode": "641001",
  "publicDescription": "Manufacturer and supplier of textile components."
}
```

### Auto-created default Expense Accounts (on `POST /api/companies`)
All created with `accountType = "Expense"`:
- EB Bill
- Rent
- Salary
- Office Supplies
- Internet
- Fuel
- Travel
- Maintenance
- Miscellaneous Expense

### Updated company detail response (`GET /api/companies/{companyId}`)
```json
{
  "companyId": 1,
  "companyName": "Astraval Trading",
  "industry": "Textile",
  "pan": "ABCDE1234F",
  "gstNo": "33ABCDE1234F1Z5",
  "hsnCode": "5205",
  "shortName": "AT",
  "fsId": "file-id-logo",
  "contactPerson": "Arun",
  "contactEmail": "sales@astraval.com",
  "contactPhone": "9999999999",
  "website": "https://astraval.com",
  "addressLine1": "No. 10 Main Road",
  "addressLine2": "Industrial Area",
  "city": "Coimbatore",
  "state": "Tamil Nadu",
  "country": "India",
  "postalCode": "641001",
  "publicDescription": "Manufacturer and supplier of textile components.",
  "isActive": true
}
```

Other related endpoint:
- `POST /api/companies/{companyId}/logo` (multipart form-data with `file`)

Common error messages:
- `Company not found with ID: {companyId}`
- `Company name is required`
- `Industry is required`
- `Logo file is required`
- `Failed to upload company logo: ...`

---

## Work Log Admin APIs

Base: `/api/companies/{companyId}/modemp/work-logs`

### New / Updated admin behavior
- Admin can update work logs even if they are already `APPROVED` or `REJECTED`
- On admin update, the work log is reset to `PENDING`
- Admin update/delete is blocked once salary is already calculated for that employee/date
- Delete is admin-only

### 1) Update Work Log by Admin
- `PUT /api/companies/{companyId}/modemp/work-logs/{logId}`

Request:
```json
{
  "employeeId": 15,
  "workDefId": 4,
  "logDate": "2026-05-11",
  "quantity": 8,
  "employeeRemarks": "Adjusted quantity after review"
}
```

### 2) Delete Work Log by Admin
- `DELETE /api/companies/{companyId}/modemp/work-logs/{logId}`

Common error messages:
- `Work log not found with ID: {logId}`
- `Cannot update this work log because salary is already calculated for {logDate}`
- `Cannot move this work log to {logDate} because salary is already calculated for that employee and date`
- `Cannot delete this work log because salary is already calculated for {logDate}`
- `Another work log already exists for the same employee, work type, and date`

---

## Mobile Integration Notes
- Use `responseData` for business payload.
- For list screens:
  - Marketplace company list -> `/api/marketplace/companies`
  - Marketplace company products -> `/api/marketplace/companies/{companyId}/items`
- For settings/org profile edit:
  - fetch -> `GET /api/companies/{companyId}`
  - update -> `PUT /api/companies/{companyId}`
- Expense flows:
  - fetch account types once -> `/expense-accounts/account-types`
  - fetch active expense accounts -> `?activeOnly=true`
  - create/update expenses using `expenseAccountId`
  - salary payment flow -> create expense with `salaryPeriodId`

## Salary + Dashboard Integration Notes
- `POST /api/companies/{companyId}/modemp/salary/calculate` only creates the salary period. It does not create an expense.
- Salary payment is recorded by creating one or more Expense rows linked through `salaryPeriodId`.
- Partial salary payments are supported.
- Salary period summary/detail now returns:
  - `paidAmount`
  - `balanceAmount`
  - `paymentCount`
  - `payments` (detail endpoint only)
- `PATCH /api/companies/{companyId}/modemp/salary/periods/{salaryPeriodId}/mark-paid` should no longer be used by clients. Use the Expense create flow instead.
- Dashboard APIs now include active records from `public.expenses` in expense/outgoing totals:
  - `/api/companies/{companyId}/analytics/dashboard/kpi`
  - `/api/companies/{companyId}/analytics/dashboard/cash-flow`
  - `/api/companies/{companyId}/analytics/dashboard/revenue-expense`
  - `/api/companies/{companyId}/analytics/dashboard/monthly-trend`
  - `/api/companies/{companyId}/analytics/dashboard/payment-mode-distribution`
