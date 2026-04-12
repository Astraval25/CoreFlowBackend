package com.astraval.coreflow.employee_module.leavelog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

import com.astraval.coreflow.employee_module.enums.LeaveCategory;
import com.astraval.coreflow.employee_module.enums.LeaveType;

@Data
public class CreateLeaveLogDto {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Leave date is required")
    private LocalDate leaveDate;

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "Leave category is required")
    private LeaveCategory leaveCategory;

    private String reason;
}
