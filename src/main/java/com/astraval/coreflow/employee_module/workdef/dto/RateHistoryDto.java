package com.astraval.coreflow.employee_module.workdef.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.astraval.coreflow.employee_module.enums.WorkUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateHistoryDto {
    private Long rateHistoryId;
    private BigDecimal ratePerUnit;
    private WorkUnit unit;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
