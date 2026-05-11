package com.astraval.coreflow.main_modules.companies.dto;

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

    private String contactPerson;

    private String contactEmail;

    private String contactPhone;

    private String website;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private String publicDescription;
}
