package com.astraval.coreflow.employee_module.leavelog.dto;

import com.astraval.coreflow.employee_module.enums.LeaveStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewLeaveLogDto {

    @NotNull(message = "Status is required")
    private LeaveStatus status;
}
