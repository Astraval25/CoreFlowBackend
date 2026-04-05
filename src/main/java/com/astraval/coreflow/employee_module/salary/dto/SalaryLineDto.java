package com.astraval.coreflow.employee_module.salary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.astraval.coreflow.employee_module.enums.SalaryLineType;
import com.astraval.coreflow.employee_module.enums.WorkUnit;

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
