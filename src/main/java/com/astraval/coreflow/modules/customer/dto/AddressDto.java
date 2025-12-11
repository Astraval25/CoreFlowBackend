package com.astraval.coreflow.modules.customer.dto;

import lombok.Data;

@Data
public class AddressDto {
    private String attentionName;
    private String country;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private Integer pincode;
    private String phone;
    private String email;
}