package com.astraval.coreflow.modules.customer.projection;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerProjection {
    private Long customerId;
    private String customerName;
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