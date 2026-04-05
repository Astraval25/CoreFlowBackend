package com.astraval.coreflow.modules.modemp.workdef.dto;

import com.astraval.coreflow.modules.modemp.enums.WorkUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateWorkDefinitionDto {

    @NotBlank(message = "Work name is required")
    private String workName;

    @NotBlank(message = "Work code is required")
    private String workCode;

    private String description;

    @NotNull(message = "Rate per unit is required")
    @PositiveOrZero(message = "Rate must be positive or zero")
    private BigDecimal ratePerUnit;

    @NotNull(message = "Unit is required")
    private WorkUnit unit;
}
