package com.astraval.coreflow.modules.modemp.leavelog.dto;

import com.astraval.coreflow.modules.modemp.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewLeaveLogDto {

    @NotNull(message = "Status is required")
    private LeaveStatus status;
}
