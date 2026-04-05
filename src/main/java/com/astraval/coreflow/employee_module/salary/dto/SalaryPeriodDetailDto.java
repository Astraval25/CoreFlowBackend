package com.astraval.coreflow.employee_module.salary.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.astraval.coreflow.employee_module.enums.SalaryPeriodStatus;
import com.astraval.coreflow.employee_module.enums.SalaryType;

@Data
public class SalaryPeriodDetailDto {
    private Long salaryPeriodId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String period;
    private LocalDate fromDate;
    private LocalDate toDate;
    private SalaryType salaryType;
    private Integer workingDaysInMonth;
    private BigDecimal daysPresent;
    private BigDecimal daysAbsent;
    private BigDecimal lopDays;
    private BigDecimal grossAmount;
    private BigDecimal lopDeduction;
    private BigDecimal otherDeductions;
    private BigDecimal netAmount;
    private SalaryPeriodStatus status;
    private Long approvedBy;
    private LocalDateTime approvedDt;
    private LocalDateTime paidDt;
    private String paymentRef;
    private LocalDateTime computedDt;
    private List<SalaryLineDto> lines;
}
