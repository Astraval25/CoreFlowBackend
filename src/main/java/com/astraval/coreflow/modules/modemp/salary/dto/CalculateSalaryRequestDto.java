package com.astraval.coreflow.modules.modemp.salary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CalculateSalaryRequestDto {

    @NotBlank(message = "Period is required (YYYYMM format)")
    private String period;

    @NotNull(message = "Working days in month is required")
    private Integer workingDaysInMonth;

    private Long employeeId;
}
