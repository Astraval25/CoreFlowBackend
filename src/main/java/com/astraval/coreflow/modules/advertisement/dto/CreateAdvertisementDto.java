package com.astraval.coreflow.modules.advertisement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAdvertisementDto {

    @NotNull
    private Long companyId;

    private Long itemId;

    @NotBlank
    @Size(max = 50)
    private String placement;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @Size(max = 500)
    private String actionUrl;
}
