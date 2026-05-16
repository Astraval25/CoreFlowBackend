package com.astraval.coreflow.main_modules.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDto {
    private Long expenseId;
    private LocalDate expenseDate;
    private String paymentMode;
    private BigDecimal amount;
    private Long expenseAccountId;
    private String expenseAccountName;
    private String expenseAccountType;
    private String invoiceNo;
    private Long vendorId;
    private String vendorName;
    private Long customerId;
    private String customerName;
    private String remark;
    private Long salaryPeriodId;
    private Boolean isActive;
    private LocalDateTime createdDt;
    private LocalDateTime lastModifiedDt;
}
