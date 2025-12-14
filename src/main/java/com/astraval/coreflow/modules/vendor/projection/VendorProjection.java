package com.astraval.coreflow.modules.vendor.projection;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}