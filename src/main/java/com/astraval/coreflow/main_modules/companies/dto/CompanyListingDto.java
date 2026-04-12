package com.astraval.coreflow.main_modules.companies.dto;

import lombok.Data;

@Data
public class CompanyListingDto {
  private long companyId;
  private String companyName;
  private String pan;
  private String gstNo;

}
