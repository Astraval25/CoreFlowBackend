package com.astraval.coreflow.modules.customer.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class UpdateCustomerRequest {
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    private String email;
    private String phone;
    private String lang;
    private String pan;
    private String gst;
    private BigDecimal advanceAmount;
    
    // Address fields
    private AddressDto billingAddress;
    private AddressDto shippingAddress;
    private Boolean sameForShipping;
}