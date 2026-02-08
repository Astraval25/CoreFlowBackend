package com.astraval.coreflow.modules.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrHealthResponse {
    private boolean available;
    private String version;
    private String error;
}
