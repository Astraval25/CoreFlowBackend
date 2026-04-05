package com.astraval.coreflow.modules.modemp.salaryconfig.dto;

import com.astraval.coreflow.modules.modemp.enums.SalaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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
