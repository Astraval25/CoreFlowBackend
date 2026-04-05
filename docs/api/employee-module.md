# Employee & Manufacturing Module

## Problem Statement

In a manufacturing business, raw materials are purchased and then processed through multiple stages by employees. Each stage is a distinct **work type** with its own pay rate per unit (kg, piece, box, etc.). Employees are of two kinds:

1. **Monthly salary** -- fixed pay, adjusted for leaves/absences
2. **Work-based salary** -- paid per unit of work completed

Employees log their daily work. At month-end the system calculates salary: attendance-based deductions for monthly employees, and summed piece-rate earnings for work-based employees.

---

## Example Flow

```
┌─────────────────────────────────────────────────────────┐
│                   EMPLOYEE WORK STAGES                  │
│                                                         │
│  1. Cutting         ── 50/kg                            │
│  2. Splitting       ── 20/kg                            │
│  3. Tying rope #1   ── 60/pc                            │
│  4. Tying rope #2   ── 60/pc                            │
│  5. Tying rope #3   ── 60/pc                            │
│  6. Packing         ── 10/box                           │
│                                                         │
│  Any employee can do any work on any day.               │
│  They log: work type + quantity + date                  │
│  System snapshots current rate → computes amount.       │
└─────────────────────────────────────────────────────────┘
```

---

## Schema Overview (modemp schema)

All manufacturing tables live in the **`modemp`** PostgreSQL schema. They reference `public.companies` for company isolation.

### ER Relationship

```
companies (public)
    │
    ├── employees
    │       ├── employee_portal_users   (login credentials)
    │       ├── employee_salary_config  (versioned salary type + amount)
    │       ├── employee_work_logs      (daily piece-rate entries)
    │       ├── employee_leave_logs     (leave/absence records)
    │       └── employee_salary_periods (month-end salary computation)
    │               └── employee_salary_lines  (line-item breakdown)
    │
    └── work_definitions
            └── work_definition_rate_history  (rate change audit)
```

---

## Tables

### 1. `modemp.employees` -- Employee Master

| Column | Type | Notes |
|---|---|---|
| employee_id | int8 PK | Auto-generated |
| company_id | int8 FK | → `public.companies` |
| employee_code | varchar(50) | **Unique per company** |
| employee_name | varchar(255) | Required |
| phone | varchar(20) | |
| email | varchar(255) | |
| designation | varchar(100) | |
| joined_dt | date | |
| is_active | bool | Default `true` |
| created_by, created_dt, modified_by, modified_dt | audit | Standard audit columns |

**Constraints:**
- `UNIQUE (company_id, employee_code)` -- no duplicate codes within a company.

---

### 2. `modemp.employee_portal_users` -- Employee Login

Separate from the main `users` table. Employees get their own lightweight portal credentials.

| Column | Type | Notes |
|---|---|---|
| portal_user_id | int8 PK | |
| employee_id | int8 FK | → `modemp.employees` |
| company_id | int8 FK | → `public.companies` |
| username | varchar(100) | **Globally unique** |
| password | varchar(300) | BCrypt hashed |
| last_login_dt | timestamp | |
| is_active | bool | |
| created_dt, modified_dt | timestamp | |

**Key design decision:** Employee login is separate from the admin `users` table. This means:
- Employees don't appear in the admin user pool
- Employee tokens/sessions can be scoped differently
- No role-mapping complexity with the existing `user_role_map`

---

### 3. `modemp.employee_salary_config` -- Versioned Salary Configuration

Instead of storing `salary_type` and `monthly_amount` directly on the employee, this table keeps a **versioned history**. The active config is the row where `effective_to IS NULL`.

| Column | Type | Notes |
|---|---|---|
| config_id | int8 PK | |
| employee_id | int8 FK | → `modemp.employees` |
| salary_type | varchar(20) | `MONTHLY` or `WORK_BASED` |
| monthly_amount | numeric(38,2) | Only for `MONTHLY` |
| effective_from | date | When this config started |
| effective_to | date | `NULL` = currently active |
| created_by, created_dt | audit | |

**Why versioned?** If an employee switches from monthly to work-based (or gets a salary revision), the old config row gets `effective_to = today` and a new row is inserted. Historical salary calculations remain accurate because they reference the config that was active at the time.

**Index:** `(employee_id, effective_from)` for fast lookup of active config.

---

### 4. `modemp.work_definitions` -- Work Types

Admin-defined catalog of work types available in the company.

| Column | Type | Notes |
|---|---|---|
| work_def_id | int8 PK | |
| company_id | int8 FK | → `public.companies` |
| work_name | varchar(255) | Display name |
| work_code | varchar(50) | **Unique per company** (e.g., `CUT`, `SPLIT`, `PACK`) |
| description | text | |
| rate_per_unit | numeric(38,2) | Current rate |
| unit | varchar(20) | `KG`, `PC`, `BOX`, `LITER`, `METER`, `GRAM`, `HOUR` |
| is_active | bool | Default `true` |
| created_by, created_dt, modified_by, modified_dt | audit | |

**Constraints:**
- `UNIQUE (company_id, work_code)`
- `CHECK (unit IN ('KG','PC','BOX','LITER','METER','GRAM','HOUR'))`

---

### 5. `modemp.work_definition_rate_history` -- Rate Change Audit

When `rate_per_unit` changes on a work definition, the old rate is archived here. Work logs keep their own `rate_snapshot`, so this table is purely for **audit**.

| Column | Type | Notes |
|---|---|---|
| rate_history_id | int8 PK | |
| work_def_id | int8 FK | → `modemp.work_definitions` |
| rate_per_unit | numeric(38,2) | The rate that was active |
| unit | varchar(20) | |
| effective_from | date | When this rate started |
| effective_to | date | When it was replaced |
| changed_by, changed_dt | audit | |

---

### 6. `modemp.employee_work_logs` -- Daily Work Entries

The core transaction table. Employees enter what they did each day.

| Column | Type | Notes |
|---|---|---|
| log_id | int8 PK | |
| employee_id | int8 FK | → `modemp.employees` |
| company_id | int8 FK | → `public.companies` |
| work_def_id | int8 FK | → `modemp.work_definitions` |
| log_date | date | The day the work was done |
| quantity | numeric(38,2) | How much (e.g., 50 kg, 200 pcs) |
| unit | varchar(20) | **Snapshot** from work definition at log time |
| rate_snapshot | numeric(38,2) | **Snapshot** of rate at log time -- never changes |
| amount_earned | numeric(38,2) | `quantity * rate_snapshot` -- stored, not computed on read |
| employee_remarks | text | Employee's notes |
| status | varchar(20) | `PENDING` → `APPROVED` / `REJECTED` |
| reviewed_by | int8 | Admin who reviewed |
| reviewed_dt | timestamp | |
| admin_remarks | text | Admin feedback |
| created_dt, modified_dt | timestamp | |

**Key design decisions:**

1. **Rate snapshot is immutable.** Even if the admin later changes the rate on the work definition, existing logs keep the rate that was active when the employee logged the work. This prevents retroactive salary changes.

2. **Approval workflow.** Logs start as `PENDING`. Admin reviews and marks `APPROVED` or `REJECTED`. Only `APPROVED` logs count toward salary.

3. **Stored amount.** `amount_earned = quantity * rate_snapshot` is stored rather than computed at query time. This makes salary aggregation fast and ensures consistency.

**Indexes:**
- `(employee_id, log_date)` -- employee's daily view
- `(company_id, log_date, status)` -- admin review queue

---

### 7. `modemp.employee_leave_logs` -- Leave Records

For **monthly salary** employees. Records absences that reduce net pay.

| Column | Type | Notes |
|---|---|---|
| leave_id | int8 PK | |
| employee_id | int8 FK | → `modemp.employees` |
| company_id | int8 FK | → `public.companies` |
| leave_date | date | |
| leave_type | varchar(20) | `FULL_DAY` or `HALF_DAY` |
| leave_category | varchar(20) | `CASUAL`, `SICK`, `UNPAID`, `LOP` |
| reason | text | |
| status | varchar(20) | `PENDING` → `APPROVED` / `REJECTED` |
| approved_by, approved_dt | audit | |
| created_dt | timestamp | |

**Constraints:**
- `UNIQUE (employee_id, leave_date)` -- one record per employee per day
- Leave categories determine payroll impact:
  - `CASUAL` / `SICK` -- may or may not deduct depending on company policy
  - `UNPAID` / `LOP` (Loss of Pay) -- always deducted from salary

---

### 8. `modemp.employee_salary_periods` -- Monthly Salary Computation

One row per employee per month. Created at month-end when salary is calculated.

| Column | Type | Notes |
|---|---|---|
| salary_period_id | int8 PK | |
| employee_id | int8 FK | → `modemp.employees` |
| company_id | int8 FK | → `public.companies` |
| period | char(6) | Format: `YYYYMM` (e.g., `202604`) |
| salary_type | varchar(20) | Snapshot: `MONTHLY` or `WORK_BASED` |
| working_days_in_month | int | Total working days configured for the month |
| days_present | numeric(5,1) | Half-days count as 0.5 |
| days_absent | numeric(5,1) | Including unapproved leaves |
| lop_days | numeric(5,1) | Loss-of-pay days (subset of absent) |
| gross_amount | numeric(38,2) | Before deductions |
| lop_deduction | numeric(38,2) | `(monthly_salary / working_days) * lop_days` |
| other_deductions | numeric(38,2) | Manual adjustments |
| net_amount | numeric(38,2) | Final payable |
| status | varchar(20) | `DRAFT` → `APPROVED` → `PAID` |
| approved_by, approved_dt | audit | |
| paid_dt | timestamp | When payment was released |
| payment_ref | varchar(100) | Bank reference / transaction ID |
| computed_dt, created_dt | timestamp | |

**Constraints:**
- `UNIQUE (employee_id, period)` -- one salary record per employee per month

**Status flow:**
```
DRAFT ──→ APPROVED ──→ PAID
  │                      
  └── (recalculate) ─┘   
```

---

### 9. `modemp.employee_salary_lines` -- Salary Breakdown

Line-item detail for each salary period. Makes the salary slip auditable.

| Column | Type | Notes |
|---|---|---|
| line_id | int8 PK | |
| salary_period_id | int8 FK | → `modemp.employee_salary_periods` |
| work_def_id | int8 FK | Nullable -- only for `WORK_EARNING` lines |
| line_type | varchar(20) | `FIXED`, `WORK_EARNING`, `DEDUCTION`, `BONUS` |
| description | varchar(255) | Human-readable label |
| total_qty | numeric(38,2) | Work-based only |
| unit | varchar(20) | Work-based only |
| rate_used | numeric(38,2) | Work-based only |
| amount | numeric(38,2) | Line amount |

**Line types explained:**

| line_type | When used | Example |
|---|---|---|
| `FIXED` | Monthly salary base | "Monthly salary: 25,000" |
| `WORK_EARNING` | Work-based earning per work type | "Cutting: 500 kg x 50/kg = 25,000" |
| `DEDUCTION` | LOP deduction, other deductions | "LOP: 2 days = -1,923" |
| `BONUS` | Any bonus/incentive | "Overtime bonus: +2,000" |

---

## Salary Calculation Logic

### Monthly Salary Employee

```
Input:
  base_salary = 25,000
  working_days = 26
  days_present = 24
  lop_days = 2

Calculation:
  per_day = 25,000 / 26 = 961.54
  lop_deduction = 961.54 * 2 = 1,923.08
  net_salary = 25,000 - 1,923.08 = 23,076.92

Salary lines:
  FIXED         "Monthly salary"      +25,000.00
  DEDUCTION     "LOP (2 days)"         -1,923.08
  ─────────────────────────────────────────────
  Net                                  23,076.92
```

### Work-Based Salary Employee

```
Input (approved work logs for the month):
  Cutting:    500 kg  x 50/kg  = 25,000
  Splitting:  300 kg  x 20/kg  =  6,000
  Packing:     50 box x 10/box =    500

Calculation:
  gross = 25,000 + 6,000 + 500 = 31,500
  deductions = 0
  net_salary = 31,500

Salary lines:
  WORK_EARNING  "Cutting"    500 kg  @50   +25,000.00
  WORK_EARNING  "Splitting"  300 kg  @20    +6,000.00
  WORK_EARNING  "Packing"     50 box @10      +500.00
  ─────────────────────────────────────────────────
  Net                                       31,500.00
```

## Data Flow: Month-End Salary Run

```
                    ┌───────────────────────┐
                    │  Admin triggers        │
                    │  "Calculate Salary"    │
                    │  for April 2026        │
                    └───────────┬───────────┘
                                │
            ┌───────────────────┴────────────────────┐
            │                                        │
   ┌────────▼─────────┐                    ┌─────────▼────────┐
   │  MONTHLY employee │                    │ WORK_BASED emp   │
   │                   │                    │                  │
   │ 1. Get active     │                    │ 1. Get APPROVED  │
   │    salary config  │                    │    work_logs for │
   │                   │                    │    this month    │
   │ 2. Count APPROVED │                    │                  │
   │    leave_logs     │                    │ 2. Group by      │
   │    for this month │                    │    work_def_id   │
   │                   │                    │                  │
   │ 3. Calculate:     │                    │ 3. Sum amounts   │
   │    LOP deduction  │                    │    per work type │
   │    = (salary/26)  │                    │                  │
   │      * lop_days   │                    │ 4. Insert salary │
   │                   │                    │    lines per     │
   │ 4. Insert salary  │                    │    work type     │
   │    period + lines │                    │                  │
   └──────────────────┘                    └──────────────────┘
            │                                        │
            └───────────────────┬────────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │  salary_periods row   │
                    │  status = DRAFT       │
                    │                       │
                    │  Admin reviews →      │
                    │  status = APPROVED    │
                    │                       │
                    │  Payment done →       │
                    │  status = PAID        │
                    └───────────────────────┘
```
