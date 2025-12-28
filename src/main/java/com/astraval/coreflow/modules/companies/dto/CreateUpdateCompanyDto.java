package com.astraval.coreflow.modules.companies.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUpdateCompanyDto {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Industry is required")
    private String industry;

    private String pan;

    private String gstNo;

    private String hsnCode;

    private String shortName;
}