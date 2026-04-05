package com.astraval.coreflow.main_modules.vendor.dto;

import java.util.List;

import com.astraval.coreflow.common.util.PaginationInfo;
import com.astraval.coreflow.main_modules.vendor.dto.VendorOrderSummaryDto;
import com.astraval.coreflow.main_modules.vendor.dto.VendorPaymentSummaryDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorOrderPaymentSummaryDto {
    private List<VendorOrderSummaryDto> orders;
    private List<VendorPaymentSummaryDto> payments;

    @JsonIgnore
    private PaginationInfo paginationInfo;

    public VendorOrderPaymentSummaryDto(List<VendorOrderSummaryDto> orders, List<VendorPaymentSummaryDto> payments) {
        this.orders = orders;
        this.payments = payments;
    }
}
