package com.astraval.coreflow.modules.modemp.employee.dto;

import com.astraval.coreflow.modules.modemp.enums.SalaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSummaryDto {
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String phone;
    private String designation;
    private Boolean isActive;
    private SalaryType currentSalaryType;
    private BigDecimal currentMonthlyAmount;
}
