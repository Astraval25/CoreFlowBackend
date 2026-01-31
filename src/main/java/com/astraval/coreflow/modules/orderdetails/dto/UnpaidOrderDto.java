package com.astraval.coreflow.modules.orderdetails.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnpaidOrderDto {
    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private String orderStatus;
    private String companyName;
    private String vendorDisplayName;
    private Long sellerCompanyId;
    private Boolean hasBill;
    private Double orderAmount;
    private Double totalAmount;
    private Double paidAmount;
    private Boolean isActive;
}