package com.astraval.coreflow.modules.modemp.portaluser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePortalUserDto {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
