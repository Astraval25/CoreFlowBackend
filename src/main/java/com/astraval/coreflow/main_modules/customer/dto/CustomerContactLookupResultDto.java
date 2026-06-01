package com.astraval.coreflow.main_modules.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerContactLookupResultDto {
    private String inputPhone;
    private String phoneKey;
    private Boolean validPhone;
    private Boolean hasAccount;
    private Long accountCompanyId;
    private String accountCompanyName;
    private Long existingCustomerId;
}
