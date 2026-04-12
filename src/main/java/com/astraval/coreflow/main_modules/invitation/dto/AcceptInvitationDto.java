package com.astraval.coreflow.main_modules.invitation.dto;

import lombok.Data;

@Data
public class AcceptInvitationDto {
    private Long selectedCustomerId;
    private Long selectedVendorId;
}
