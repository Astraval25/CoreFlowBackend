package com.astraval.coreflow.employee_module.salary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPaymentDto {
    private Long expenseId;
    private LocalDate expenseDate;
    private String paymentMode;
    private BigDecimal amount;
    private String invoiceNo;
    private String remark;
}
