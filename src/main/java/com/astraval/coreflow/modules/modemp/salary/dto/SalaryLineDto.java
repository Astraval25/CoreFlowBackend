package com.astraval.coreflow.modules.modemp.salary.dto;

import com.astraval.coreflow.modules.modemp.enums.SalaryLineType;
import com.astraval.coreflow.modules.modemp.enums.WorkUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryLineDto {
    private Long lineId;
    private SalaryLineType lineType;
    private String description;
    private BigDecimal totalQty;
    private WorkUnit unit;
    private BigDecimal rateUsed;
    private BigDecimal amount;
    private Long workDefId;
    private String workName;
}
