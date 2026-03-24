# Company Customization Configuration

## Overview

Each company on the CoreFlow platform can customize various aspects of their experience. The customization system uses two tables:

1. **`config_definition`** — platform-wide table defining all available config keys with their default values. Managed by admins. No redeploy needed to change defaults.
2. **`company_config`** — sparse table storing only the overrides per company. If a company hasn't overridden a key, the platform default from `config_definition` is used.

```
Resolution order:  company_config (override)  →  config_definition (default)
```

---

## Tables

### `config_definition` (Platform Defaults)

One row per config key. Defines what configs exist, their defaults, data types, and descriptions. This is the **single source of truth for all default values** — no defaults are hardcoded in Java or SQL.

```sql
CREATE TABLE IF NOT EXISTS config_definition (
    config_definition_id BIGSERIAL PRIMARY KEY,
    config_key           VARCHAR(100) NOT NULL UNIQUE,
    default_value        VARCHAR(500) NOT NULL,
    data_type            VARCHAR(20)  NOT NULL DEFAULT 'STRING',  -- STRING, INTEGER, BOOLEAN, DECIMAL
    category             VARCHAR(50),                             -- grouping: 'NUMBERING', 'DISPLAY', 'TAX', etc.
    description          VARCHAR(500),
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,

    created_dt           TIMESTAMP DEFAULT NOW(),
    modified_dt          TIMESTAMP
);
```

**Seed data** (inserted once at deployment):

```sql
INSERT INTO config_definition (config_key, default_value, data_type, category, description) VALUES
-- Number format
('sales_order_prefix',    'SO',                       'STRING',  'NUMBERING', 'Prefix for sales order numbers'),
('purchase_order_prefix', 'PO',                       'STRING',  'NUMBERING', 'Prefix for purchase order numbers'),
('payment_out_prefix',    'PAY',                      'STRING',  'NUMBERING', 'Prefix for outgoing payment numbers (buyer side)'),
('payment_in_prefix',     'REC',                      'STRING',  'NUMBERING', 'Prefix for incoming payment numbers (seller side)'),
('number_format',         '{PREFIX}-{MMYYYY}-{SEQ}',  'STRING',  'NUMBERING', 'Format template for all number generation'),
('seq_padding',           '0',                        'INTEGER', 'NUMBERING', 'Zero-pad sequence number (0 = no padding, 4 = 0005)')
ON CONFLICT (config_key) DO NOTHING;
```

### `company_config` (Company Overrides)

Sparse table — only rows where a company has explicitly overridden a default. If no row exists for a `(company_id, config_key)` pair, the platform default from `config_definition` is used.

```sql
CREATE TABLE IF NOT EXISTS company_config (
    company_config_id BIGSERIAL PRIMARY KEY,
    company_id        BIGINT NOT NULL REFERENCES companies(comp_id),
    config_key        VARCHAR(100) NOT NULL REFERENCES config_definition(config_key),
    config_value      VARCHAR(500) NOT NULL,

    -- Audit
    created_by        BIGINT,
    created_dt        TIMESTAMP DEFAULT NOW(),
    modified_by       BIGINT,
    modified_dt       TIMESTAMP,

    UNIQUE(company_id, config_key)
);
```

### Resolution Logic

```
Has company_config row for (company_id, config_key)?
  YES → use company_config.config_value
  NO  → use config_definition.default_value
```

**In SQL:**

```sql
-- Get effective config value for a company
SELECT COALESCE(
    (SELECT config_value FROM company_config
     WHERE company_id = p_company_id AND config_key = 'sales_order_prefix'),
    (SELECT default_value FROM config_definition
     WHERE config_key = 'sales_order_prefix')
) INTO v_prefix;
```

**In Java:**

```java
public String getConfig(Long companyId, String key) {
    return companyConfigRepo
        .findByCompanyCompanyIdAndConfigKey(companyId, key)
        .map(CompanyConfig::getConfigValue)
        .orElseGet(() -> configDefinitionRepo
            .findByConfigKey(key)
            .map(ConfigDefinition::getDefaultValue)
            .orElse(null));
}
```

### Example State

```
config_definition:
┌──────────────────────┬───────────────────────────┐
│ config_key           │ default_value             │
├──────────────────────┼───────────────────────────┤
│ sales_order_prefix   │ SO                        │
│ purchase_order_prefix│ PO                        │
│ number_format        │ {PREFIX}-{MMYYYY}-{SEQ}   │
│ seq_padding          │ 0                         │
└──────────────────────┴───────────────────────────┘

company_config (only overrides):
┌────────────┬──────────────────────┬──────────────────────────┐
│ company_id │ config_key           │ config_value             │
├────────────┼──────────────────────┼──────────────────────────┤
│ 2          │ sales_order_prefix   │ INV                      │
│ 2          │ seq_padding          │ 4                        │
│ 3          │ number_format        │ {PREFIX}/{YYYY}/{MM}/{SEQ}│
└────────────┴──────────────────────┴──────────────────────────┘

Effective values:
  Company 1: SO-032026-5          (all defaults, no overrides)
  Company 2: INV-032026-0005      (custom prefix + padding)
  Company 3: SO/2026/03/5         (custom format, default prefix)
```

---

## 1. Order / Payment Number Format Customization

### Config Keys

| Config Key | Default Value | Data Type | Description |
|---|---|---|---|
| `sales_order_prefix` | `SO` | STRING | Prefix for sales order numbers |
| `purchase_order_prefix` | `PO` | STRING | Prefix for purchase order numbers |
| `payment_out_prefix` | `PAY` | STRING | Prefix for outgoing payment numbers (buyer side) |
| `payment_in_prefix` | `REC` | STRING | Prefix for incoming payment numbers (seller side) |
| `number_format` | `{PREFIX}-{MMYYYY}-{SEQ}` | STRING | Format template for all number generation |
| `seq_padding` | `0` | INTEGER | Zero-pad the sequence number (0 = no padding, 4 = "0005") |

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
| `{SEQ}` | Auto-incrementing sequence number (per company, per type, per period) | `5` or `0005` |

### Examples

**Company A** — no overrides (all defaults):
```
sales_order_prefix  = "SO"       (from config_definition)
number_format       = "{PREFIX}-{MMYYYY}-{SEQ}"  (from config_definition)
seq_padding         = 0          (from config_definition)
Result              = "SO-032026-5"
```

**Company B** — overrides prefix and padding:
```
sales_order_prefix  = "INV"      (from company_config)
number_format       = "{PREFIX}-{MMYYYY}-{SEQ}"  (from config_definition, not overridden)
seq_padding         = 4          (from company_config)
Result              = "INV-032026-0005"
```

**Company C** — overrides format:
```
sales_order_prefix  = "SO"       (from config_definition, not overridden)
number_format       = "{PREFIX}/{YYYY}/{MM}/{SEQ}"  (from company_config)
seq_padding         = 0          (from config_definition, not overridden)
Result              = "SO/2026/03/5"
```

### Sequence Reset Period

The sequence counter resets based on the period tokens in the format:
- If format contains `{MMYYYY}` or `{YYYYMM}` or `{MM}` → resets monthly
- If format contains only `{YYYY}` or `{YY}` → resets yearly
- If format has no date tokens → never resets (continuous)

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

### SQL Function

The `generate_company_number()` function reads from both tables — company override first, then platform default:

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
    -- Determine which prefix key to look up based on type
    v_prefix_key := CASE p_type
        WHEN 'SALES_ORDER'    THEN 'sales_order_prefix'
        WHEN 'PURCHASE_ORDER' THEN 'purchase_order_prefix'
        WHEN 'PAYMENT_OUT'    THEN 'payment_out_prefix'
        WHEN 'PAYMENT_IN'     THEN 'payment_in_prefix'
    END;

    -- Resolve prefix: company override → platform default
    SELECT COALESCE(
        (SELECT config_value FROM company_config
         WHERE company_id = p_company_id AND config_key = v_prefix_key),
        (SELECT default_value FROM config_definition
         WHERE config_key = v_prefix_key)
    ) INTO v_prefix;

    -- Resolve format: company override → platform default
    SELECT COALESCE(
        (SELECT config_value FROM company_config
         WHERE company_id = p_company_id AND config_key = 'number_format'),
        (SELECT default_value FROM config_definition
         WHERE config_key = 'number_format')
    ) INTO v_format;

    -- Resolve padding: company override → platform default
    SELECT COALESCE(
        (SELECT config_value::INT FROM company_config
         WHERE company_id = p_company_id AND config_key = 'seq_padding'),
        (SELECT default_value::INT FROM config_definition
         WHERE config_key = 'seq_padding')
    ) INTO v_padding;

    -- Determine period for sequence grouping
    v_period := TO_CHAR(NOW(), 'MMYYYY');

    -- Get next sequence value (atomic upsert)
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

---

## Java Entities

### `ConfigDefinition`

```java
@Entity
@Table(name = "config_definition")
public class ConfigDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_definition_id")
    private Long configDefinitionId;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "default_value", nullable = false, length = 500)
    private String defaultValue;

    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType;  // STRING, INTEGER, BOOLEAN, DECIMAL

    @Column(name = "category", length = 50)
    private String category;  // NUMBERING, DISPLAY, TAX, etc.

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @Column(name = "modified_dt")
    private LocalDateTime modifiedDt;
}
```

### `CompanyConfig`

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

### `CompanyConfigService`

```java
@Service
public class CompanyConfigService {

    @Autowired
    private CompanyConfigRepository companyConfigRepo;

    @Autowired
    private ConfigDefinitionRepository configDefinitionRepo;

    /**
     * Get effective config value for a company.
     * Resolution: company_config (override) → config_definition (default)
     */
    public String getConfig(Long companyId, String key) {
        return companyConfigRepo
            .findByCompanyCompanyIdAndConfigKey(companyId, key)
            .map(CompanyConfig::getConfigValue)
            .orElseGet(() -> configDefinitionRepo
                .findByConfigKey(key)
                .map(ConfigDefinition::getDefaultValue)
                .orElse(null));
    }

    /**
     * Get all effective configs for a company (defaults + overrides merged).
     */
    public Map<String, String> getAllConfigs(Long companyId) {
        // Start with all defaults
        Map<String, String> result = configDefinitionRepo.findAll().stream()
            .collect(Collectors.toMap(
                ConfigDefinition::getConfigKey,
                ConfigDefinition::getDefaultValue
            ));

        // Apply company overrides on top
        companyConfigRepo.findByCompanyCompanyId(companyId)
            .forEach(cc -> result.put(cc.getConfigKey(), cc.getConfigValue()));

        return result;
    }

    /**
     * Set a company-specific override. Creates or updates.
     */
    public void setConfig(Long companyId, String key, String value, Companies company) {
        CompanyConfig config = companyConfigRepo
            .findByCompanyCompanyIdAndConfigKey(companyId, key)
            .orElseGet(() -> {
                CompanyConfig c = new CompanyConfig();
                c.setCompany(company);
                c.setConfigKey(key);
                return c;
            });
        config.setConfigValue(value);
        companyConfigRepo.save(config);
    }

    /**
     * Remove a company override (revert to platform default).
     */
    public void resetToDefault(Long companyId, String key) {
        companyConfigRepo
            .findByCompanyCompanyIdAndConfigKey(companyId, key)
            .ifPresent(companyConfigRepo::delete);
    }
}
```

---

## 2. Future Customization Candidates

These are areas in the current codebase where company-level customization could be added. Each uses the same two-table pattern — add a row to `config_definition` for the default, companies override via `company_config`.

### Confirmed Patterns (already exist as hardcoded values)

| Area | Current State | Config Keys |
|---|---|---|
| **Order number format** | Hardcoded `ORD-MMYYYY-SEQ` in SQL | `sales_order_prefix`, `purchase_order_prefix`, `number_format`, `seq_padding` |
| **Payment number format** | Hardcoded `PAY-MMYYYY-SEQ` in SQL | `payment_out_prefix`, `payment_in_prefix` (shares `number_format`) |
| **Email templates** | Global `email_templates` table | Could add company_id FK for company-specific templates |

### Potential Future Candidates (need confirmation)

| Area | What it would do | Config Keys | Notes |
|---|---|---|---|
| **Currency** | Set company's currency (no currency field exists currently) | `currency_code` (`INR`, `USD`), `currency_symbol` (`₹`, `$`) | Simple key-value config |
| **Tax defaults** | Company-level default tax rate, auto-populates new items | `default_tax_rate` (`18`), `tax_label` (`GST`, `VAT`) | Simple key-value config |
| **Date format** | Display format for dates in exports/PDFs | `date_format` (`DD/MM/YYYY`, `MM-DD-YYYY`) | Simple key-value config |
| **Auto-numbering scope** | Control when sequences reset | `seq_reset_period` (`MONTHLY`, `YEARLY`, `NEVER`) | Simple key-value config |
| **Due amount thresholds** | Alert when due amount exceeds a limit | `due_amount_alert_threshold` | Simple key-value config |
| **Order statuses** | Custom workflow statuses beyond hardcoded ones | Needs separate `company_order_status` table | Too complex for key-value |
| **Payment modes** | Predefined list instead of free-text | Needs separate `company_payment_mode` table | Too complex for key-value |

### Adding a New Config

To add a new customization:

1. Insert a row into `config_definition`:
   ```sql
   INSERT INTO config_definition (config_key, default_value, data_type, category, description)
   VALUES ('currency_code', 'INR', 'STRING', 'DISPLAY', 'Company currency ISO code');
   ```

2. No code changes needed for resolution — `CompanyConfigService.getConfig()` already handles it.

3. Companies that want a different value insert into `company_config`:
   ```sql
   INSERT INTO company_config (company_id, config_key, config_value)
   VALUES (5, 'currency_code', 'USD');
   ```

No schema changes. No redeployment. No migration scripts.
