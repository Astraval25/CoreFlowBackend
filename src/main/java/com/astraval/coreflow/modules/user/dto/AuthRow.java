package com.astraval.coreflow.modules.user.dto;

import com.astraval.coreflow.modules.companies.Companies;

public record AuthRow(
                Long userId,
                String email,
                String userName,
                String password,
                String roleCode,
                Companies companies
        ) {
}
