package com.astraval.coreflow.employee_module.leavelog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.astraval.coreflow.employee_module.enums.LeaveCategory;
import com.astraval.coreflow.employee_module.enums.LeaveStatus;
import com.astraval.coreflow.employee_module.enums.LeaveType;

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
