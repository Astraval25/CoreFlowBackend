package com.astraval.coreflow.modules.notification.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FcmSendResultDto {

    private int totalTargets;
    private int successCount;
    private int failureCount;
    private List<String> failedTokens;
}
