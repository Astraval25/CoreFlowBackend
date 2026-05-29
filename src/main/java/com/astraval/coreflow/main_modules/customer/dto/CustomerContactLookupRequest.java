package com.astraval.coreflow.main_modules.customer.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CustomerContactLookupRequest {
    private List<String> phones = new ArrayList<>();
}
