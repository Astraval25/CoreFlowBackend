package com.astraval.coreflow.modules.modemp.worklog.dto;

import com.astraval.coreflow.modules.modemp.enums.WorkLogStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewWorkLogDto {

    @NotNull(message = "Status is required")
    private WorkLogStatus status;

    private String adminRemarks;
}
