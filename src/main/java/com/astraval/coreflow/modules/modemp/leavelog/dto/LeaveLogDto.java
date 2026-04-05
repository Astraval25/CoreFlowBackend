package com.astraval.coreflow.modules.modemp.leavelog.dto;

import com.astraval.coreflow.modules.modemp.enums.LeaveCategory;
import com.astraval.coreflow.modules.modemp.enums.LeaveStatus;
import com.astraval.coreflow.modules.modemp.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveLogDto {
    private Long leaveId;
    private Long employeeId;
    private String employeeName;
    private LocalDate leaveDate;
    private LeaveType leaveType;
    private LeaveCategory leaveCategory;
    private String reason;
    private LeaveStatus status;
    private Long approvedBy;
    private LocalDateTime approvedDt;
}
