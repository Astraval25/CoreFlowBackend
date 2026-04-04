package com.astraval.coreflow.modules.customer.dto;

import java.util.List;

import com.astraval.coreflow.common.util.PaginationInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderPaymentSummaryDto {
    private List<CustomerOrderSummaryDto> orders;
    private List<CustomerPaymentSummaryDto> payments;

    @JsonIgnore
    private PaginationInfo paginationInfo;

    public CustomerOrderPaymentSummaryDto(List<CustomerOrderSummaryDto> orders, List<CustomerPaymentSummaryDto> payments) {
        this.orders = orders;
        this.payments = payments;
    }
}
