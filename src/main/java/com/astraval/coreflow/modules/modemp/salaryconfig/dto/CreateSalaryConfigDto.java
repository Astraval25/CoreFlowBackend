package com.astraval.coreflow.modules.modemp.salaryconfig.dto;

import com.astraval.coreflow.modules.modemp.enums.SalaryType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateSalaryConfigDto {

    @NotNull(message = "Salary type is required")
    private SalaryType salaryType;

    private BigDecimal monthlyAmount;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;
}
