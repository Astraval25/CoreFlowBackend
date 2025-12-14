package com.astraval.coreflow.modules.address.projection;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AddressProjection {
    private Integer addressId;
    private String attentionName;
    private String country;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private Integer pincode;
    private String phone;
    private String email;
    private Boolean isActive;
    private LocalDateTime createdDt;
    private LocalDateTime modifiedDt;
}