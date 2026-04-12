package com.astraval.coreflow.employee_module.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.astraval.coreflow.employee_module.enums.SalaryType;

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
