package com.astraval.coreflow.employee_module.salaryconfig.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.astraval.coreflow.employee_module.enums.SalaryType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryConfigDto {
    private Long configId;
    private SalaryType salaryType;
    private BigDecimal monthlyAmount;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
