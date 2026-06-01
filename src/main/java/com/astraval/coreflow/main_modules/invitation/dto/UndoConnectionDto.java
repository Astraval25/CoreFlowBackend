package com.astraval.coreflow.main_modules.invitation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UndoConnectionDto {
    private String newStatus; // "ACCEPTED" or "REJECTED"
}
