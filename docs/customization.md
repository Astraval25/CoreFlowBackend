# Company Customization Configuration

## Overview

Each company on the CoreFlow platform can customize various aspects of their experience. All company-level configuration is stored in a single `company_config` table, designed to be extended with new config keys over time without schema changes.

---

## `company_config` Table

```sql
CREATE TABLE IF NOT EXISTS company_config (
    company_config_id BIGSERIAL PRIMARY KEY,
    company_id        BIGINT NOT NULL REFERENCES companies(comp_id),
    config_key        VARCHAR(100) NOT NULL,
    config_value      VARCHAR(500) NOT NULL,
    description       VARCHAR(255),

    -- Audit
    created_by        BIGINT,
    created_dt        TIMESTAMP DEFAULT NOW(),
    modified_by       BIGINT,
    modified_dt       TIMESTAMP,

    UNIQUE(company_id, config_key)
);
```

### Design Rationale

- **Key-value pair**: Each config is a `(company_id, config_key, config_value)` tuple. New customizations can be added by inserting new keys — no ALTER TABLE needed.
- **Defaults**: If a company has no row for a given `config_key`, the application uses a platform default. This means the table is sparse — only companies that override defaults have rows.
- **Typed in code**: The Java service layer interprets `config_value` based on the `config_key` (e.g., parse as String for prefixes, Integer for sequence padding).

---

## 1. Order / Payment Number Format Customization

### Config Keys

| Config Key | Default Value | Description |
|---|---|---|
| `sales_order_prefix` | `SO` | Prefix for sales order numbers |
| `purchase_order_prefix` | `PO` | Prefix for purchase order numbers |
| `payment_out_prefix` | `PAY` | Prefix for outgoing payment numbers (buyer side) |
| `payment_in_prefix` | `REC` | Prefix for incoming payment numbers (seller side) |
| `number_format` | `{PREFIX}-{MMYYYY}-{SEQ}` | Format template for all number generation |
| `seq_padding` | `0` | Zero-pad the sequence number (0 = no padding, 4 = "0005") |

### Format Tokens

The `number_format` template supports these tokens:

| Token | Resolves to | Example |
|---|---|---|
| `{PREFIX}` | The relevant prefix config (e.g., `sales_order_prefix`) | `SO` |
| `{YYYY}` | 4-digit year | `2026` |
| `{YY}` | 2-digit year | `26` |
| `{MM}` | 2-digit month | `03` |
| `{MMYYYY}` | Month + year | `032026` |
| `{YYYYMM}` | Year + month | `202603` |
| `{DD}` | 2-digit day | `24` |
| `{SEQ}` | Auto-incrementing sequence number (per company, per period) | `5` or `0005` |

### Examples

**Company A** — default config:
```
sales_order_prefix  = "SO"
number_format       = "{PREFIX}-{MMYYYY}-{SEQ}"
Result              = "SO-032026-5"
```

**Company B** — custom config:
```
sales_order_prefix  = "INV"
number_format       = "{PREFIX}/{YYYY}/{MM}/{SEQ}"
seq_padding         = 4
Result              = "INV/2026/03/0012"
```

**Company C** — minimal:
```
purchase_order_prefix = "PUR"
number_format         = "{PREFIX}-{SEQ}"
Result                = "PUR-42"
```

### Sequence Reset Period

The sequence counter resets based on the period tokens in the format:
- If format contains `{MMYYYY}` or `{YYYYMM}` or `{MM}` → resets monthly
- If format contains only `{YYYY}` or `{YY}` → resets yearly
- If format has no date tokens → never resets (continuous)

This is managed by the existing `company_order_sequence` / `company_payment_sequence` tables, with the period key derived from the format.

### SQL Function Update

The `generate_order_number()` and `generate_payment_number()` functions would be updated to:

```sql
CREATE OR REPLACE FUNCTION generate_company_number(
    p_company_id BIGINT,
    p_type VARCHAR(20)  -- 'SALES_ORDER', 'PURCHASE_ORDER', 'PAYMENT_OUT', 'PAYMENT_IN'
)
RETURNS TEXT AS $$
DECLARE
    v_prefix TEXT;
    v_format TEXT;
    v_padding INT;
    v_period TEXT;
    v_next BIGINT;
    v_result TEXT;
    v_prefix_key TEXT;
BEGIN
    -- Determine prefix config key based on type
    v_prefix_key := CASE p_type
        WHEN 'SALES_ORDER'    THEN 'sales_order_prefix'
        WHEN 'PURCHASE_ORDER' THEN 'purchase_order_prefix'
        WHEN 'PAYMENT_OUT'    THEN 'payment_out_prefix'
        WHEN 'PAYMENT_IN'     THEN 'payment_in_prefix'
    END;

    -- Read company config (with defaults)
    SELECT COALESCE(
        (SELECT config_value FROM company_config
         WHERE company_id = p_company_id AND config_key = v_prefix_key),
        CASE p_type
            WHEN 'SALES_ORDER'    THEN 'SO'
            WHEN 'PURCHASE_ORDER' THEN 'PO'
            WHEN 'PAYMENT_OUT'    THEN 'PAY'
            WHEN 'PAYMENT_IN'     THEN 'REC'
        END
    ) INTO v_prefix;

    SELECT COALESCE(
        (SELECT config_value FROM company_config
         WHERE company_id = p_company_id AND config_key = 'number_format'),
        '{PREFIX}-{MMYYYY}-{SEQ}'
    ) INTO v_format;

    SELECT COALESCE(
        (SELECT config_value::INT FROM company_config
         WHERE company_id = p_company_id AND config_key = 'seq_padding'),
        0
    ) INTO v_padding;

    -- Determine period for sequence grouping
    v_period := TO_CHAR(NOW(), 'MMYYYY');

    -- Get next sequence (reuses existing sequence tables)
    -- Use a unified sequence table keyed by (company_id, type, period)
    INSERT INTO company_number_sequence(company_id, number_type, period, last_value)
    VALUES (p_company_id, p_type, v_period, 1)
    ON CONFLICT (company_id, number_type, period)
    DO UPDATE SET last_value = company_number_sequence.last_value + 1
    RETURNING last_value INTO v_next;

    -- Build result from format template
    v_result := v_format;
    v_result := REPLACE(v_result, '{PREFIX}', v_prefix);
    v_result := REPLACE(v_result, '{YYYY}', TO_CHAR(NOW(), 'YYYY'));
    v_result := REPLACE(v_result, '{YY}', TO_CHAR(NOW(), 'YY'));
    v_result := REPLACE(v_result, '{MM}', TO_CHAR(NOW(), 'MM'));
    v_result := REPLACE(v_result, '{DD}', TO_CHAR(NOW(), 'DD'));
    v_result := REPLACE(v_result, '{MMYYYY}', TO_CHAR(NOW(), 'MMYYYY'));
    v_result := REPLACE(v_result, '{YYYYMM}', TO_CHAR(NOW(), 'YYYYMM'));

    IF v_padding > 0 THEN
        v_result := REPLACE(v_result, '{SEQ}', LPAD(v_next::TEXT, v_padding, '0'));
    ELSE
        v_result := REPLACE(v_result, '{SEQ}', v_next::TEXT);
    END IF;

    RETURN v_result;
END;
$$ LANGUAGE plpgsql;
```

### Unified Sequence Table

Replace the separate `company_order_sequence` and `company_payment_sequence` tables with one:

```sql
CREATE TABLE IF NOT EXISTS company_number_sequence (
    company_id   BIGINT,
    number_type  VARCHAR(20),   -- 'SALES_ORDER', 'PURCHASE_ORDER', 'PAYMENT_OUT', 'PAYMENT_IN'
    period       CHAR(6),
    last_value   BIGINT,
    PRIMARY KEY (company_id, number_type, period)
);
```

---

## 2. Future Customization Candidates

These are areas in the current codebase where company-level customization could be added. Each uses the same `company_config` table — just new `config_key` entries.

### Confirmed Patterns (already exist as hardcoded values)

| Area | Current State | Proposed Config Keys |
|---|---|---|
| **Order number format** | Hardcoded `ORD-MMYYYY-SEQ` in SQL | `sales_order_prefix`, `purchase_order_prefix`, `number_format`, `seq_padding` |
| **Payment number format** | Hardcoded `PAY-MMYYYY-SEQ` in SQL | `payment_out_prefix`, `payment_in_prefix` (shares `number_format`) |
| **Email templates** | Global `email_templates` table | Could add company_id FK for company-specific templates |

### Potential Future Candidates (need confirmation)

| Area | What it would do | Config Keys |
|---|---|---|
| **Currency** | No currency field exists; all amounts are unitless. Allow companies to set their currency. | `currency_code` (e.g., `INR`, `USD`), `currency_symbol` (e.g., `₹`, `$`) |
| **Tax defaults** | Tax rate is set per-item. A company-level default tax rate would auto-populate. | `default_tax_rate` (e.g., `18`), `tax_label` (e.g., `GST`, `VAT`) |
| **Date format** | API returns ISO dates. Companies may want display format customization. | `date_format` (e.g., `DD/MM/YYYY`, `MM-DD-YYYY`) |
| **Order statuses** | Hardcoded strings (`QUOTATION`, `INVOICED`, `DELIVERED`, etc.). Companies may want custom workflow statuses. | Would need a separate `company_order_status` table rather than a simple config key |
| **Payment modes** | Free-text `mode_of_payment`. Companies may want a predefined list. | Would need a separate `company_payment_mode` table |
| **Auto-numbering scope** | Sequences reset monthly. Some companies may want yearly or never-reset. | `seq_reset_period` (e.g., `MONTHLY`, `YEARLY`, `NEVER`) |
| **Due amount thresholds** | Alert when a customer/vendor's due amount exceeds a threshold. | `due_amount_alert_threshold` |

---

## Java Entity (for reference)

```java
@Entity
@Table(name = "company_config",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "config_key"}))
@EntityListeners(AuditingEntityListener.class)
public class CompanyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_config_id")
    private Long companyConfigId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 500)
    private String configValue;

    @Column(name = "description", length = 255)
    private String description;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @LastModifiedBy
    @Column(name = "modified_by")
    private Long lastModifiedBy;

    @LastModifiedDate
    @Column(name = "modified_dt")
    private LocalDateTime lastModifiedDt;
}
```

### Service Usage Pattern

```java
@Service
public class CompanyConfigService {

    @Autowired
    private CompanyConfigRepository configRepository;

    private static final Map<String, String> DEFAULTS = Map.of(
        "sales_order_prefix", "SO",
        "purchase_order_prefix", "PO",
        "payment_out_prefix", "PAY",
        "payment_in_prefix", "REC",
        "number_format", "{PREFIX}-{MMYYYY}-{SEQ}",
        "seq_padding", "0"
    );

    public String getConfig(Long companyId, String key) {
        return configRepository
            .findByCompanyCompanyIdAndConfigKey(companyId, key)
            .map(CompanyConfig::getConfigValue)
            .orElse(DEFAULTS.get(key));
    }

    public void setConfig(Long companyId, String key, String value) {
        CompanyConfig config = configRepository
            .findByCompanyCompanyIdAndConfigKey(companyId, key)
            .orElse(new CompanyConfig());
        config.setCompany(/* resolve company */);
        config.setConfigKey(key);
        config.setConfigValue(value);
        configRepository.save(config);
    }
}
```
