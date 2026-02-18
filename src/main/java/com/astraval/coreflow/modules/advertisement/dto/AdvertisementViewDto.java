package com.astraval.coreflow.modules.advertisement.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AdvertisementViewDto {
    private Long adId;
    private Long companyId;
    private String companyName;
    private Long itemId;
    private String itemName;
    private String placement;
    private String description;
    private String actionUrl;
    private String fsId;
    private Boolean isActive;
    private LocalDateTime createdDt;
}
