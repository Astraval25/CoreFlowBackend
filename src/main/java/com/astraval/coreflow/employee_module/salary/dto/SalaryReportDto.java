package com.astraval.coreflow.employee_module.salary.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SalaryReportDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private int totalEmployees;
    private BigDecimal totalGrossAmount;
    private BigDecimal totalDeductions;
    private BigDecimal totalNetAmount;
    private List<SalaryPeriodDetailDto> salaryDetails;
}
