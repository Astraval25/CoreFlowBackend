package com.astraval.coreflow.employee_module.workdef.dto;

import lombok.Data;

import java.math.BigDecimal;

import com.astraval.coreflow.employee_module.enums.WorkUnit;

@Data
public class UpdateWorkDefinitionDto {
    private String workName;
    private String description;
    private BigDecimal ratePerUnit;
    private WorkUnit unit;
}
