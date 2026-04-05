package com.astraval.coreflow.modules.modemp.salary.dto;

import com.astraval.coreflow.modules.modemp.enums.SalaryPeriodStatus;
import com.astraval.coreflow.modules.modemp.enums.SalaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPeriodSummaryDto {
    private Long salaryPeriodId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String period;
    private SalaryType salaryType;
    private BigDecimal grossAmount;
    private BigDecimal netAmount;
    private SalaryPeriodStatus status;
}
