package com.astraval.coreflow.main_modules.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUpdateExpenseDto {

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Expense account is required")
    private Long expenseAccountId;

    private String invoiceNo;

    private Long vendorId;

    private Long customerId;

    private String remark;

    private Long salaryPeriodId;
}
