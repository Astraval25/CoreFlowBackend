package com.astraval.coreflow.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateNotificationRequest {

    private Long userId;

    private Long fromCompanyId;

    @NotNull
    private Long toCompanyId;

    @NotBlank
    @Size(max = 160)
    private String title;

    @NotBlank
    @Size(max = 1000)
    private String message;

    @NotBlank
    @Size(max = 50)
    private String type;

    @Size(max = 120)
    private String actionLabel;

    @Size(max = 500)
    private String actionUrl;
}
