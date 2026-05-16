package com.astraval.coreflow.main_modules.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryDto {
    private Long customerId;
    private String displayName;
    private String customerCompanyName;
    private String email;
    private Double dueAmount;
    private Boolean isActive;
    private Long unreadCount;

    public CustomerSummaryDto(
            Long customerId,
            String displayName,
            String customerCompanyName,
            String email,
            Double dueAmount,
            Boolean isActive) {
        this.customerId = customerId;
        this.displayName = displayName;
        this.customerCompanyName = customerCompanyName;
        this.email = email;
        this.dueAmount = dueAmount;
        this.isActive = isActive;
        this.unreadCount = 0L;
    }
}
