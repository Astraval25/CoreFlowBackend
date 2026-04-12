package com.astraval.coreflow.employee_module.salaryconfig.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.astraval.coreflow.employee_module.enums.SalaryType;

@Data
public class CreateSalaryConfigDto {

    @NotNull(message = "Salary type is required")
    private SalaryType salaryType;

    private BigDecimal monthlyAmount;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;
}
