package com.astraval.coreflow.modules.modemp.workdef.dto;

import com.astraval.coreflow.modules.modemp.enums.WorkUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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
