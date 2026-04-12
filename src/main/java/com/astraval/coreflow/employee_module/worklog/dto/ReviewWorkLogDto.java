package com.astraval.coreflow.employee_module.worklog.dto;

import com.astraval.coreflow.employee_module.enums.WorkLogStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewWorkLogDto {

    @NotNull(message = "Status is required")
    private WorkLogStatus status;

    private String adminRemarks;
}
