package com.astraval.coreflow.main_modules.invitation.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequestDetailDto {
    private Long inviteId;
    private Long fromCompanyId;
    private String fromCompanyName;
    private String fromCompanyPhone;
    private String fromCompanyEmail;
    private String fromCompanyIndustry;
    private String requestedEntityType;
    private Long requestedEntityId;
    private String status;
    private LocalDateTime createdAt;
}
