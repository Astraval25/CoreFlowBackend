package com.astraval.coreflow.employee_module.workdef.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.astraval.coreflow.employee_module.enums.WorkUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkDefinitionDto {
    private Long workDefId;
    private String workName;
    private String workCode;
    private String description;
    private BigDecimal ratePerUnit;
    private WorkUnit unit;
    private Boolean isActive;
}
