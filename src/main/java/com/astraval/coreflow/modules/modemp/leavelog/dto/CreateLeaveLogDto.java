package com.astraval.coreflow.modules.modemp.leavelog.dto;

import com.astraval.coreflow.modules.modemp.enums.LeaveCategory;
import com.astraval.coreflow.modules.modemp.enums.LeaveType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

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
