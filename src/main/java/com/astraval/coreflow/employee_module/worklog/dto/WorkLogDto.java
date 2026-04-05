package com.astraval.coreflow.employee_module.worklog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.astraval.coreflow.employee_module.enums.WorkLogStatus;
import com.astraval.coreflow.employee_module.enums.WorkUnit;

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
