package com.astraval.coreflow.main_modules.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorSummaryDto {
    private Long vendorId;
    private String displayName;
    private String vendorCompanyName;
    private String email;
    private Double dueAmount;
    private Boolean isActive;
    private String connectionStatus;
    private Long unreadCount;

    public VendorSummaryDto(
            Long vendorId,
            String displayName,
            String vendorCompanyName,
            String email,
            Double dueAmount,
            Boolean isActive,
            String connectionStatus) {
        this.vendorId = vendorId;
        this.displayName = displayName;
        this.vendorCompanyName = vendorCompanyName;
        this.email = email;
        this.dueAmount = dueAmount;
        this.isActive = isActive;
        this.connectionStatus = connectionStatus;
        this.unreadCount = 0L;
    }
}
