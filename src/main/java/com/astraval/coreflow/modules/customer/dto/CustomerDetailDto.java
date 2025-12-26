package com.astraval.coreflow.modules.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record CustomerDetailDto(
        Long customerId,
        String customerName,
        String displayName,
        String email,
        String phone,
        String lang,
        String pan,
        String gst,
        BigDecimal advanceAmount,
        Boolean isActive,
        LocalDateTime createdDt) {
}