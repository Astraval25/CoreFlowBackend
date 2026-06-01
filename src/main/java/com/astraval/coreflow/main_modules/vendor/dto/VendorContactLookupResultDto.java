package com.astraval.coreflow.main_modules.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorContactLookupResultDto {
    private String inputPhone;
    private String phoneKey;
    private Boolean validPhone;
    private Boolean hasAccount;
    private Long accountCompanyId;
    private String accountCompanyName;
    private Long existingVendorId;
}
