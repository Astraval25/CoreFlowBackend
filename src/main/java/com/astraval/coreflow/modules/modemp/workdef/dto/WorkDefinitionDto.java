package com.astraval.coreflow.modules.modemp.workdef.dto;

import com.astraval.coreflow.modules.modemp.enums.WorkUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
