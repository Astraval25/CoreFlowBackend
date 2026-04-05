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
  "period": "202604",
  "workingDaysInMonth": 26,
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
      "salaryType": "MONTHLY",
      "grossAmount": 28000,
      "netAmount": 26923.08,
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
    "salaryType": "MONTHLY",
    "workingDaysInMonth": 26,
    "daysPresent": 25,
    "daysAbsent": 1,
    "lopDays": 1,
    "grossAmount": 28000,
    "lopDeduction": 1076.92,
    "otherDeductions": 0,
    "netAmount": 26923.08,
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
        "description": "Monthly salary",
        "totalQty": null,
        "unit": null,
        "rateUsed": null,
        "amount": 28000,
        "workDefId": null,
        "workName": null
      },
      {
        "lineId": 1002,
        "lineType": "DEDUCTION",
        "description": "LOP deduction",
        "totalQty": 1,
        "unit": "HOUR",
        "rateUsed": 1076.92,
        "amount": 1076.92,
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
