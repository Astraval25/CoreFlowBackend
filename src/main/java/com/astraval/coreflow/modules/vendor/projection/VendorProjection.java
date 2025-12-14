package com.astraval.coreflow.modules.vendor.projection;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.astraval.coreflow.modules.address.projection.AddressProjection;

@Data
public class VendorProjection {
    private Long vendorId;
    private String vendorName;
    private String displayName;
    private String email;
    private String phone;
    private String lang;
    private String pan;
    private String gst;
    private BigDecimal advanceAmount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private AddressProjection billingAddress;
    private AddressProjection shippingAddress;
}