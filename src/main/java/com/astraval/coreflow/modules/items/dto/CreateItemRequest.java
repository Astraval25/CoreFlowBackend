package com.astraval.coreflow.modules.items.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class CreateItemRequest {
    private CreateItemDto item;
    private MultipartFile file;
}