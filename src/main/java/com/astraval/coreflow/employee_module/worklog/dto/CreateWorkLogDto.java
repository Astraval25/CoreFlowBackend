package com.astraval.coreflow.employee_module.worklog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateWorkLogDto {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Work definition ID is required")
    private Long workDefId;

    @NotNull(message = "Log date is required")
    private LocalDate logDate;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    private String employeeRemarks;
}
