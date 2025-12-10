package com.astraval.coreflow.modules.companies.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminCompaniesResponseDto {
    private String companyId;
    private String companyName;
}
