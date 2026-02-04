package com.astraval.coreflow.modules.invitation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationViewDto {
    private Long inviteId;
    private UUID invitationCode;
    private String status;
    private String requestedEntityType;
    private Long requestedEntityId;
    private Long fromCompanyId;
    private String fromCompanyName;
    private Long toCompanyId;
    private String email;
    private LocalDateTime sendAt;
    private LocalDateTime accespedAt;
}
