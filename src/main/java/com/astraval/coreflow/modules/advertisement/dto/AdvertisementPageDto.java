package com.astraval.coreflow.modules.advertisement.dto;

import java.util.List;

import lombok.Data;

@Data
public class AdvertisementPageDto {
    private List<AdvertisementViewDto> advertisements;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}
