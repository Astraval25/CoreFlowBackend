package com.astraval.coreflow.main_modules.vendor.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class VendorContactLookupRequest {
    private List<String> phones = new ArrayList<>();
}
