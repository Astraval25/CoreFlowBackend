package com.astraval.coreflow.modules.modemp.portaluser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortalUserDto {
    private Long portalUserId;
    private Long employeeId;
    private String username;
    private Boolean isActive;
    private LocalDateTime lastLoginDt;
}
