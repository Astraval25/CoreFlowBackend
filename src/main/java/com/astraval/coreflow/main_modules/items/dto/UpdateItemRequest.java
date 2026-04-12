package com.astraval.coreflow.main_modules.items.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class UpdateItemRequest {
    private UpdateItemDto item;
    private MultipartFile file;
}