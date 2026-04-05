# Employee Module APIs (`modemp`)

Base prefix: `/api/companies/{companyId}/modemp`

## Common Response Wrapper
All endpoints return:

```json
{
  "responseStatus": true,
  "responseCode": 200,
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

## Enum Values
- `SalaryType`: `MONTHLY`, `WORK_BASED`
- `WorkUnit`: `KG`, `PC`, `BOX`, `LITER`, `METER`, `GRAM`, `HOUR`
- `WorkLogStatus`: `PENDING`, `APPROVED`, `REJECTED`
- `LeaveType`: `FULL_DAY`, `HALF_DAY`
- `LeaveCategory`: `CASUAL`, `SICK`, `UNPAID`, `LOP`
- `LeaveStatus`: `PENDING`, `APPROVED`, `REJECTED`
- `SalaryPeriodStatus`: `DRAFT`, `APPROVED`, `PAID`
- `SalaryLineType`: `FIXED`, `WORK_EARNING`, `DEDUCTION`, `BONUS`

## Employee APIs

### 1. Create Employee
- API: `POST /api/companies/{companyId}/modemp/employees`
- Request example:

```http
POST /api/companies/1/modemp/employees
Content-Type: application/json
```

```json
{
  "employeeCode": "EMP-001",
  "employeeName": "Ravi Kumar",
  "phone": "9876543210",
  "email": "ravi@company.com",
  "designation": "Machine Operator",
  "joinedDt": "2026-01-10",
  "salaryType": "MONTHLY",
  "monthlyAmount": 25000
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 201,
  "responseMessage": "Employee created successfully",
  "responseData": {
    "employeeId": 101
  }
}
```

### 2. Get Employees
- API: `GET /api/companies/{companyId}/modemp/employees?activeOnly={boolean}`
- Request example:

```http
GET /api/companies/1/modemp/employees?activeOnly=true
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Employees retrieved successfully",
  "responseData": [
    {
      "employeeId": 101,
      "employeeCode": "EMP-001",
      "employeeName": "Ravi Kumar",
      "phone": "9876543210",
      "designation": "Machine Operator",
      "isActive": true,
      "currentSalaryType": "MONTHLY",
      "currentMonthlyAmount": 25000
    }
  ]
}
```

### 3. Get Employee Detail
- API: `GET /api/companies/{companyId}/modemp/employees/{employeeId}`
- Request example:

```http
GET /api/companies/1/modemp/employees/101
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Employee retrieved successfully",
  "responseData": {
    "employeeId": 101,
    "employeeCode": "EMP-001",
    "employeeName": "Ravi Kumar",
    "phone": "9876543210",
    "email": "ravi@company.com",
    "designation": "Machine Operator",
    "joinedDt": "2026-01-10",
    "isActive": true,
    "currentSalaryType": "MONTHLY",
    "currentMonthlyAmount": 25000,
    "salaryConfigHistory": [
      {
        "configId": 501,
        "salaryType": "MONTHLY",
        "monthlyAmount": 25000,
        "effectiveFrom": "2026-01-10",
        "effectiveTo": null
      }
    ],
    "createdBy": 1,
    "createdDt": "2026-01-10T10:00:00",
    "lastModifiedBy": 1,
    "lastModifiedDt": "2026-01-10T10:00:00"
  }
}
```

### 4. Update Employee
- API: `PUT /api/companies/{companyId}/modemp/employees/{employeeId}`
- Request example:

```http
PUT /api/companies/1/modemp/employees/101
Content-Type: application/json
```

```json
{
  "employeeName": "Ravi Kumar S",
  "phone": "9988776655",
  "email": "ravi.s@company.com",
  "designation": "Senior Operator",
  "joinedDt": "2026-01-10"
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 203,
  "responseMessage": "Employee updated successfully",
  "responseData": null
}
```

### 5. Deactivate Employee
- API: `PATCH /api/companies/{companyId}/modemp/employees/{employeeId}/deactivate`
- Request example:

```http
PATCH /api/companies/1/modemp/employees/101/deactivate
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 203,
  "responseMessage": "Employee deactivated successfully",
  "responseData": null
}
```

## Salary Config APIs

### 6. Create Salary Config
- API: `POST /api/companies/{companyId}/modemp/employees/{employeeId}/salary-config`
- Request example:

```http
POST /api/companies/1/modemp/employees/101/salary-config
Content-Type: application/json
```

```json
{
  "salaryType": "MONTHLY",
  "monthlyAmount": 28000,
  "effectiveFrom": "2026-04-01"
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 201,
  "responseMessage": "Salary config created successfully",
  "responseData": {
    "configId": 502
  }
}
```

### 7. Get Active Salary Config
- API: `GET /api/companies/{companyId}/modemp/employees/{employeeId}/salary-config`
- Request example:

```http
GET /api/companies/1/modemp/employees/101/salary-config
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Salary config retrieved successfully",
  "responseData": {
    "configId": 502,
    "salaryType": "MONTHLY",
    "monthlyAmount": 28000,
    "effectiveFrom": "2026-04-01",
    "effectiveTo": null
  }
}
```

### 8. Get Salary Config History
- API: `GET /api/companies/{companyId}/modemp/employees/{employeeId}/salary-config/history`
- Request example:

```http
GET /api/companies/1/modemp/employees/101/salary-config/history
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Salary config history retrieved successfully",
  "responseData": [
    {
      "configId": 501,
      "salaryType": "MONTHLY",
      "monthlyAmount": 25000,
      "effectiveFrom": "2026-01-10",
      "effectiveTo": "2026-03-31"
    },
    {
      "configId": 502,
      "salaryType": "MONTHLY",
      "monthlyAmount": 28000,
      "effectiveFrom": "2026-04-01",
      "effectiveTo": null
    }
  ]
}
```

## Portal User APIs

### 9. Create Portal User
- API: `POST /api/companies/{companyId}/modemp/employees/{employeeId}/portal-user`
- Request example:

```http
POST /api/companies/1/modemp/employees/101/portal-user
Content-Type: application/json
```

```json
{
  "username": "ravi.emp",
  "password": "Ravi@123"
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 201,
  "responseMessage": "Portal user created successfully",
  "responseData": {
    "portalUserId": 301
  }
}
```

### 10. Get Portal User
- API: `GET /api/companies/{companyId}/modemp/employees/{employeeId}/portal-user`
- Request example:

```http
GET /api/companies/1/modemp/employees/101/portal-user
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Portal user retrieved successfully",
  "responseData": {
    "portalUserId": 301,
    "employeeId": 101,
    "username": "ravi.emp",
    "isActive": true,
    "lastLoginDt": "2026-04-02T08:45:10"
  }
}
```

### 11. Reset Portal User Password
- API: `PATCH /api/companies/{companyId}/modemp/employees/{employeeId}/portal-user/reset-password`
- Request example:

```http
PATCH /api/companies/1/modemp/employees/101/portal-user/reset-password
Content-Type: application/json
```

```json
{
  "password": "New@Password123"
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 203,
  "responseMessage": "Password reset successfully",
  "responseData": null
}
```

## Work Definition APIs

### 12. Create Work Definition
- API: `POST /api/companies/{companyId}/modemp/work-definitions`
- Request example:

```http
POST /api/companies/1/modemp/work-definitions
Content-Type: application/json
```

```json
{
  "workName": "Cutting",
  "workCode": "CUT",
  "description": "Raw bundle cutting",
  "ratePerUnit": 50,
  "unit": "KG"
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 201,
  "responseMessage": "Work definition created successfully",
  "responseData": {
    "workDefId": 201
  }
}
```

### 13. Get Work Definitions
- API: `GET /api/companies/{companyId}/modemp/work-definitions?activeOnly={boolean}`
- Request example:

```http
GET /api/companies/1/modemp/work-definitions?activeOnly=true
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Work definitions retrieved successfully",
  "responseData": [
    {
      "workDefId": 201,
      "workName": "Cutting",
      "workCode": "CUT",
      "description": "Raw bundle cutting",
      "ratePerUnit": 50,
      "unit": "KG",
      "isActive": true
    }
  ]
}
```

### 14. Get Work Definition Detail
- API: `GET /api/companies/{companyId}/modemp/work-definitions/{workDefId}`
- Request example:

```http
GET /api/companies/1/modemp/work-definitions/201
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Work definition retrieved successfully",
  "responseData": {
    "workDefId": 201,
    "workName": "Cutting",
    "workCode": "CUT",
    "description": "Raw bundle cutting",
    "ratePerUnit": 50,
    "unit": "KG",
    "isActive": true
  }
}
```

### 15. Update Work Definition
- API: `PUT /api/companies/{companyId}/modemp/work-definitions/{workDefId}`
- Request example:

```http
PUT /api/companies/1/modemp/work-definitions/201
Content-Type: application/json
```

```json
{
  "workName": "Cutting - Grade A",
  "description": "Updated spec for grade A",
  "ratePerUnit": 55,
  "unit": "KG"
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 203,
  "responseMessage": "Work definition updated successfully",
  "responseData": null
}
```

### 16. Deactivate Work Definition
- API: `PATCH /api/companies/{companyId}/modemp/work-definitions/{workDefId}/deactivate`
- Request example:

```http
PATCH /api/companies/1/modemp/work-definitions/201/deactivate
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 203,
  "responseMessage": "Work definition deactivated successfully",
  "responseData": null
}
```

### 17. Get Rate History
- API: `GET /api/companies/{companyId}/modemp/work-definitions/{workDefId}/rate-history`
- Request example:

```http
GET /api/companies/1/modemp/work-definitions/201/rate-history
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Rate history retrieved successfully",
  "responseData": [
    {
      "rateHistoryId": 401,
      "ratePerUnit": 50,
      "unit": "KG",
      "effectiveFrom": "2026-01-01",
      "effectiveTo": "2026-03-31"
    },
    {
      "rateHistoryId": 402,
      "ratePerUnit": 55,
      "unit": "KG",
      "effectiveFrom": "2026-04-01",
      "effectiveTo": null
    }
  ]
}
```

## Work Log APIs

### 18. Create Work Log
- API: `POST /api/companies/{companyId}/modemp/work-logs`
- Request example:

```http
POST /api/companies/1/modemp/work-logs
Content-Type: application/json
```

```json
{
  "employeeId": 101,
  "workDefId": 201,
  "logDate": "2026-04-03",
  "quantity": 125.5,
  "employeeRemarks": "Completed morning shift"
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 201,
  "responseMessage": "Work log created successfully",
  "responseData": {
    "logId": 701
  }
}
```

- Validation rules:
  - Work log creation is blocked if salary is already calculated for that employee on `logDate`.
  - This applies to both admin and employee initiated create requests.
  - Use admin salary-adjustment API (with reason) to make post-salary corrections.

- Locked-date error example:

```json
{
  "responseStatus": false,
  "responseCode": 406,
  "responseMessage": "Cannot create work log for 2026-04-03 because salary is already calculated for this date. Please Contact the admin to adjustment  salary .",
  "responseData": null
}
```

### 19. Get Work Logs by Company Date Range
- API: `GET /api/companies/{companyId}/modemp/work-logs?from={YYYY-MM-DD}&to={YYYY-MM-DD}`
- Request example:

```http
GET /api/companies/1/modemp/work-logs?from=2026-04-01&to=2026-04-30
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Work logs retrieved successfully",
  "responseData": [
    {
      "logId": 701,
      "employeeId": 101,
      "employeeName": "Ravi Kumar",
      "workDefId": 201,
      "workName": "Cutting",
      "logDate": "2026-04-03",
      "quantity": 125.5,
      "unit": "KG",
      "rateSnapshot": 55,
      "amountEarned": 6902.5,
      "employeeRemarks": "Completed morning shift",
      "status": "PENDING",
      "reviewedBy": null,
      "reviewedDt": null,
      "adminRemarks": null
    }
  ]
}
```

### 20. Get Work Logs by Employee Date Range
- API: `GET /api/companies/{companyId}/modemp/work-logs/employee/{employeeId}?from={YYYY-MM-DD}&to={YYYY-MM-DD}`
- Request example:

```http
GET /api/companies/1/modemp/work-logs/employee/101?from=2026-04-01&to=2026-04-30
```

- Response example: same shape as API #19.

### 21. Get Pending Work Logs
- API: `GET /api/companies/{companyId}/modemp/work-logs/pending`
- Request example:

```http
GET /api/companies/1/modemp/work-logs/pending
```

- Response example: same `WorkLogDto[]` shape as API #19, filtered to `status = PENDING`.

### 22. Review Work Log
- API: `PATCH /api/companies/{companyId}/modemp/work-logs/{logId}/review`
- Request example:

```http
PATCH /api/companies/1/modemp/work-logs/701/review
Content-Type: application/json
```

```json
{
  "status": "APPROVED",
  "adminRemarks": "Validated quantity"
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 203,
  "responseMessage": "Work log reviewed successfully",
  "responseData": null
}
```

## Leave Log APIs

### 23. Create Leave Log
- API: `POST /api/companies/{companyId}/modemp/leave-logs`
- Request example:

```http
POST /api/companies/1/modemp/leave-logs
Content-Type: application/json
```

```json
{
  "employeeId": 101,
  "leaveDate": "2026-04-07",
  "leaveType": "FULL_DAY",
  "leaveCategory": "SICK",
  "reason": "Fever"
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 201,
  "responseMessage": "Leave log created successfully",
  "responseData": {
    "leaveId": 801
  }
}
```

- Validation rules:
  - Leave log creation is blocked if salary is already calculated for that employee on `leaveDate`.
  - This applies to both admin and employee initiated create requests.
  - Use admin salary-adjustment API (with reason) to make post-salary corrections.

- Locked-date error example:

```json
{
  "responseStatus": false,
  "responseCode": 406,
  "responseMessage": "Cannot create leave log for 2026-04-07 because salary is already calculated for this date. Please Contact the admin to adjustment  salary .",
  "responseData": null
}
```

### 24. Get Leave Logs by Company Date Range
- API: `GET /api/companies/{companyId}/modemp/leave-logs?from={YYYY-MM-DD}&to={YYYY-MM-DD}`
- Request example:

```http
GET /api/companies/1/modemp/leave-logs?from=2026-04-01&to=2026-04-30
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Leave logs retrieved successfully",
  "responseData": [
    {
      "leaveId": 801,
      "employeeId": 101,
      "employeeName": "Ravi Kumar",
      "leaveDate": "2026-04-07",
      "leaveType": "FULL_DAY",
      "leaveCategory": "SICK",
      "reason": "Fever",
      "status": "PENDING",
      "approvedBy": null,
      "approvedDt": null
    }
  ]
}
```

### 25. Get Leave Logs by Employee Date Range
- API: `GET /api/companies/{companyId}/modemp/leave-logs/employee/{employeeId}?from={YYYY-MM-DD}&to={YYYY-MM-DD}`
- Request example:

```http
GET /api/companies/1/modemp/leave-logs/employee/101?from=2026-04-01&to=2026-04-30
```

- Response example: same shape as API #24.

### 26. Get Pending Leave Logs
- API: `GET /api/companies/{companyId}/modemp/leave-logs/pending`
- Request example:

```http
GET /api/companies/1/modemp/leave-logs/pending
```

- Response example: same `LeaveLogDto[]` shape as API #24, filtered to `status = PENDING`.

### 27. Review Leave Log
- API: `PATCH /api/companies/{companyId}/modemp/leave-logs/{leaveId}/review`
- Request example:

```http
PATCH /api/companies/1/modemp/leave-logs/801/review
Content-Type: application/json
```

```json
{
  "status": "APPROVED"
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 203,
  "responseMessage": "Leave log reviewed successfully",
  "responseData": null
}
```

## Salary APIs

### 28. Calculate Salary
- API: `POST /api/companies/{companyId}/modemp/salary/calculate`
- Request example:

```http
POST /api/companies/1/modemp/salary/calculate
Content-Type: application/json
```

```json
{
  "fromDate": "2026-04-01",
  "toDate": "2026-04-15",
  "employeeId": 101
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 201,
  "responseMessage": "Salary calculated for 1 employee(s)",
  "responseData": {
    "salaryPeriodIds": [901]
  }
}
```

- Validation rules:
  - `fromDate` must be <= `toDate`
  - `fromDate` cannot be in the future
  - Date range must not overlap with any existing salary period for the same employee
  - If an exact same date range exists as DRAFT, it will be replaced (recalculated)
  - Employee must be active and have an active salary config

- Overlap error example:

```json
{
  "responseStatus": false,
  "responseCode": 406,
  "responseMessage": "Salary for employee Ravi Kumar overlaps with existing period 2026-04-01 to 2026-04-15 (status: DRAFT)",
  "responseData": null
}
```

- Conflict response example (DB-level duplicate):

```json
{
  "responseStatus": false,
  "responseCode": 409,
  "responseMessage": "Salary already calculated for employee ID 1 from 2026-04-01 to 2026-04-15",
  "responseData": null
}
```

### 29. Get Salary Periods
- API: `GET /api/companies/{companyId}/modemp/salary/periods?period={YYYYMM}`
- Request example:

```http
GET /api/companies/1/modemp/salary/periods?period=202604
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Salary periods retrieved successfully",
  "responseData": [
    {
      "salaryPeriodId": 901,
      "employeeId": 101,
      "employeeName": "Ravi Kumar",
      "employeeCode": "EMP-001",
      "period": "202604",
      "fromDate": "2026-04-01",
      "toDate": "2026-04-15",
      "salaryType": "MONTHLY",
      "grossAmount": 14000,
      "netAmount": 13066.67,
      "status": "DRAFT"
    }
  ]
}
```

### 30. Get Salary Period Detail
- API: `GET /api/companies/{companyId}/modemp/salary/periods/{salaryPeriodId}`
- Request example:

```http
GET /api/companies/1/modemp/salary/periods/901
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Salary period detail retrieved successfully",
  "responseData": {
    "salaryPeriodId": 901,
    "employeeId": 101,
    "employeeName": "Ravi Kumar",
    "employeeCode": "EMP-001",
    "period": "202604",
    "fromDate": "2026-04-01",
    "toDate": "2026-04-15",
    "salaryType": "MONTHLY",
    "workingDaysInMonth": 15,
    "daysPresent": 14,
    "daysAbsent": 1,
    "lopDays": 1,
    "grossAmount": 14000.00,
    "lopDeduction": 933.33,
    "otherDeductions": 0,
    "netAmount": 13066.67,
    "status": "APPROVED",
    "approvedBy": 1,
    "approvedDt": "2026-04-30T19:00:00",
    "paidDt": null,
    "paymentRef": null,
    "computedDt": "2026-04-30T18:30:00",
    "lines": [
      {
        "lineId": 1001,
        "lineType": "FIXED",
        "description": "Monthly salary (15/30 days)",
        "totalQty": 15,
        "unit": null,
        "rateUsed": 933.3333,
        "amount": 14000.00,
        "workDefId": null,
        "workName": null
      },
      {
        "lineId": 1002,
        "lineType": "DEDUCTION",
        "description": "LOP deduction (1 days)",
        "totalQty": 1,
        "unit": null,
        "rateUsed": 933.3333,
        "amount": -933.33,
        "workDefId": null,
        "workName": null
      }
    ]
  }
}
```

### 31. Approve Salary Period
- API: `PATCH /api/companies/{companyId}/modemp/salary/periods/{salaryPeriodId}/approve`
- Request example:

```http
PATCH /api/companies/1/modemp/salary/periods/901/approve
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 203,
  "responseMessage": "Salary period approved successfully",
  "responseData": null
}
```

### 32. Mark Salary Period as Paid
- API: `PATCH /api/companies/{companyId}/modemp/salary/periods/{salaryPeriodId}/mark-paid`
- Request example (with body):

```http
PATCH /api/companies/1/modemp/salary/periods/901/mark-paid
Content-Type: application/json
```

```json
{
  "paymentRef": "UTR-20260430-9981"
}
```

- Request example (without body):

```http
PATCH /api/companies/1/modemp/salary/periods/901/mark-paid
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 203,
  "responseMessage": "Salary period marked as paid",
  "responseData": null
}
```

### 33. Get Salary Report
- API: `GET /api/companies/{companyId}/modemp/salary/report?from={YYYY-MM-DD}&to={YYYY-MM-DD}`
- Request example:

```http
GET /api/companies/1/modemp/salary/report?from=2026-04-01&to=2026-04-30
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Salary report retrieved successfully",
  "responseData": {
    "fromDate": "2026-04-01",
    "toDate": "2026-04-30",
    "totalEmployees": 2,
    "totalGrossAmount": 42000.00,
    "totalDeductions": 933.33,
    "totalNetAmount": 41066.67,
    "salaryDetails": [
      {
        "salaryPeriodId": 901,
        "employeeId": 101,
        "employeeName": "Ravi Kumar",
        "employeeCode": "EMP-001",
        "period": "202604",
        "fromDate": "2026-04-01",
        "toDate": "2026-04-30",
        "salaryType": "MONTHLY",
        "grossAmount": 28000.00,
        "netAmount": 27066.67,
        "status": "DRAFT",
        "lines": [ "..." ]
      }
    ]
  }
}
```

### 34. Download Salary Slip (PDF)
- API: `GET /api/companies/{companyId}/modemp/salary/periods/{salaryPeriodId}/slip`
- Request example:

```http
GET /api/companies/1/modemp/salary/periods/901/slip
```

- Response: Binary PDF file (`application/pdf`) with `Content-Disposition: attachment; filename=salary-slip-901.pdf`

## Employee Auth APIs

### 35. Employee Login
- API: `POST /api/auth/employee/login`
- Auth: **No JWT required** (public endpoint)
- No `companyId` needed — username is globally unique, company is resolved automatically.
- Request example:

```http
POST /api/auth/employee/login
Content-Type: application/json
```

```json
{
  "username": "ravi.emp",
  "password": "Ravi@123"
}
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Employee login successful",
  "responseData": {
    "token": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "employeeId": 101,
    "employeeName": "Ravi Kumar",
    "employeeCode": "EMP-001",
    "companyId": 1,
    "companyName": "Acme Corp",
    "designation": "Machine Operator"
  }
}
```

- Notes:
  - The returned JWT contains `role: "EMP"`, `employeeId`, `companyId` claims
  - Employee tokens can **only** access `/api/emp/**` endpoints (not admin/company APIs)
  - Employee and portal user must both be active

### 36. Refresh Token (Unified — handles both admin and employee)
- API: `POST /api/auth/refresh-token`
- Auth: **No JWT required** (public endpoint)
- **SECURITY**: A single endpoint handles both user and employee token refresh.
  The `type` claim inside the refresh token determines which flow runs:
  - `type: "USER"` (or missing) → returns `LoginResponse` (admin/user token)
  - `type: "EMP"` → returns `EmployeeLoginResponse` (employee token)
  - **An employee refresh token can NEVER produce an admin token**, and vice versa.
- Request example:

```http
POST /api/auth/refresh-token
Content-Type: application/json
```

```json
{
  "refreshToken": "eyJhbGciOi..."
}
```

- Response for user token: `LoginResponse` (same as login API #login)
- Response for employee token: `EmployeeLoginResponse` (same shape as API #35)

## Employee Portal APIs (Self-Service)

Base prefix: `/api/emp`
Auth: **Requires ROLE_EMP JWT** (from employee login)
All endpoints automatically scope to the logged-in employee — no `companyId` or `employeeId` in the URL.

### 37. Get My Profile
- API: `GET /api/emp/me`
- Request example:

```http
GET /api/emp/me
Authorization: Bearer <employee-jwt>
```

- Response example:

```json
{
  "responseStatus": true,
  "responseCode": 202,
  "responseMessage": "Profile retrieved successfully",
  "responseData": {
    "employeeId": 101,
    "employeeCode": "EMP-001",
    "employeeName": "Ravi Kumar",
    "phone": "9876543210",
    "email": "ravi@company.com",
    "designation": "Machine Operator",
    "joinedDt": "2026-01-10",
    "isActive": true,
    "currentSalaryType": "MONTHLY",
    "currentMonthlyAmount": 28000,
    "salaryConfigHistory": [ "..." ]
  }
}
```

### 38. Get My Salary Periods
- API: `GET /api/emp/salary/periods?period={YYYYMM}`
- Request example:

```http
GET /api/emp/salary/periods?period=202604
Authorization: Bearer <employee-jwt>
```

- Response example: same shape as API #29, filtered to the logged-in employee only.

### 39. Get My Salary Detail
- API: `GET /api/emp/salary/periods/{salaryPeriodId}`
- Request example:

```http
GET /api/emp/salary/periods/901
Authorization: Bearer <employee-jwt>
```

- Response example: same shape as API #30.
- Returns `403` if the salary period does not belong to the logged-in employee.

### 40. Download My Salary Slip (PDF)
- API: `GET /api/emp/salary/periods/{salaryPeriodId}/slip`
- Request example:

```http
GET /api/emp/salary/periods/901/slip
Authorization: Bearer <employee-jwt>
```

- Response: Binary PDF file. Returns `403` if the salary period does not belong to the logged-in employee.

### 41. Get My Work Logs
- API: `GET /api/emp/work-logs?from={YYYY-MM-DD}&to={YYYY-MM-DD}`
- Request example:

```http
GET /api/emp/work-logs?from=2026-04-01&to=2026-04-30
Authorization: Bearer <employee-jwt>
```

- Response example: same `WorkLogDto[]` shape as API #19, filtered to the logged-in employee.

### 42. Get My Leave Logs
- API: `GET /api/emp/leave-logs?from={YYYY-MM-DD}&to={YYYY-MM-DD}`
- Request example:

```http
GET /api/emp/leave-logs?from=2026-04-01&to=2026-04-30
Authorization: Bearer <employee-jwt>
```

- Response example: same `LeaveLogDto[]` shape as API #24, filtered to the logged-in employee.
