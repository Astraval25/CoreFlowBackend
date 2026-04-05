package com.astraval.coreflow.modules.modemp.worklog.dto;

import com.astraval.coreflow.modules.modemp.enums.WorkLogStatus;
import com.astraval.coreflow.modules.modemp.enums.WorkUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkLogDto {
    private Long logId;
    private Long employeeId;
    private String employeeName;
    private Long workDefId;
    private String workName;
    private LocalDate logDate;
    private BigDecimal quantity;
    private WorkUnit unit;
    private BigDecimal rateSnapshot;
    private BigDecimal amountEarned;
    private String employeeRemarks;
    private WorkLogStatus status;
    private Long reviewedBy;
    private LocalDateTime reviewedDt;
    private String adminRemarks;
}
