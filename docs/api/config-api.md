# Config API

Company-specific configuration overrides. Each company can override platform defaults for numbering formats, prefixes, etc.

## Base URL

```
/api/companies/{companyId}/config
```

---

## 1. Get All Resolved Configs

Returns all config keys with resolved values (company override or platform default).

```
GET /api/companies/{companyId}/config
```

### Response

```json
{
  "status": 200,
  "message": "Configs retrieved",
  "data": {
    "sales_order_prefix": "SO",
    "purchase_order_prefix": "PO",
    "payment_out_prefix": "PAY",
    "payment_in_prefix": "REC",
    "number_format": "{PREFIX}-{MMYYYY}-{SEQ}",
    "seq_padding": "0"
  }
}
```

---

## 2. Set Config Override

Sets a company-specific override for a config key. Creates if not exists, updates if already set.

```
PUT /api/companies/{companyId}/config/{configKey}
```

### Request Body

```json
{
  "configValue": "INV"
}
```

### Response

```json
{
  "status": 200,
  "message": "Config updated",
  "data": "Updated"
}
```

### Available Config Keys

| Key | Default | Description |
|-----|---------|-------------|
| `sales_order_prefix` | `SO` | Prefix for sales order numbers |
| `purchase_order_prefix` | `PO` | Prefix for purchase order numbers |
| `payment_out_prefix` | `PAY` | Prefix for outgoing payment numbers |
| `payment_in_prefix` | `REC` | Prefix for incoming payment numbers |
| `number_format` | `{PREFIX}-{MMYYYY}-{SEQ}` | Format template |
| `seq_padding` | `0` | Zero-pad sequence (0=none, 4=0005) |

### Format Tokens

| Token | Example | Description |
|-------|---------|-------------|
| `{PREFIX}` | `SO` | Resolved prefix for the number type |
| `{MMYYYY}` | `032026` | Month and year |
| `{YYYYMM}` | `202603` | Year and month |
| `{YYYY}` | `2026` | Four-digit year |
| `{YY}` | `26` | Two-digit year |
| `{MM}` | `03` | Two-digit month |
| `{DD}` | `24` | Two-digit day |
| `{SEQ}` | `5` or `0005` | Sequence number (padding based on `seq_padding`) |

---

## 3. Reset Config to Default

Removes the company override, reverting to the platform default.

```
DELETE /api/companies/{companyId}/config/{configKey}
```

### Response

```json
{
  "status": 200,
  "message": "Config reset to default",
  "data": "Reset"
}
```

---

## Example Flow

1. Company 5 wants sales orders numbered as `INV-2026/03-0005` instead of `SO-032026-5`
2. Set overrides:
   - `PUT /api/companies/5/config/sales_order_prefix` with `{"configValue": "INV"}`
   - `PUT /api/companies/5/config/number_format` with `{"configValue": "{PREFIX}-{YYYY}/{MM}-{SEQ}"}`
   - `PUT /api/companies/5/config/seq_padding` with `{"configValue": "4"}`
3. Next sales order for company 5 will be numbered: `INV-2026/03-0005`
