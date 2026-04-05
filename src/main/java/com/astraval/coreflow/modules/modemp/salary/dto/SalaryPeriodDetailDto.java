package com.astraval.coreflow.modules.modemp.salary.dto;

import com.astraval.coreflow.modules.modemp.enums.SalaryPeriodStatus;
import com.astraval.coreflow.modules.modemp.enums.SalaryType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SalaryPeriodDetailDto {
    private Long salaryPeriodId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String period;
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
