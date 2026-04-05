package com.astraval.coreflow.modules.modemp.workdef.dto;

import com.astraval.coreflow.modules.modemp.enums.WorkUnit;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateWorkDefinitionDto {
    private String workName;
    private String description;
    private BigDecimal ratePerUnit;
    private WorkUnit unit;
}
