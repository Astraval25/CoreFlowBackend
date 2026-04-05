package com.astraval.coreflow.employee_module.salary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.astraval.coreflow.employee_module.enums.SalaryPeriodStatus;
import com.astraval.coreflow.employee_module.enums.SalaryType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPeriodSummaryDto {
    private Long salaryPeriodId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String period;
    private LocalDate fromDate;
    private LocalDate toDate;
    private SalaryType salaryType;
    private BigDecimal grossAmount;
    private BigDecimal netAmount;
    private SalaryPeriodStatus status;
}
