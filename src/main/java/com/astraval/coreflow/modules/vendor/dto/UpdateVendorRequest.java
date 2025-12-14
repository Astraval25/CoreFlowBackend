package com.astraval.coreflow.modules.vendor.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

import com.astraval.coreflow.modules.customer.dto.AddressDto;

@Data
public class UpdateVendorRequest {
    @NotBlank(message = "Vendor name is required")
    private String vendorName;
    
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