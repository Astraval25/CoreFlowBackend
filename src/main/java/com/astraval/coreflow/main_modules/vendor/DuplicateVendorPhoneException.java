package com.astraval.coreflow.main_modules.vendor;

public class DuplicateVendorPhoneException extends RuntimeException {

    private final Long existingVendorId;
    private final String phoneKey;

    public DuplicateVendorPhoneException(String message, Long existingVendorId, String phoneKey) {
        super(message);
        this.existingVendorId = existingVendorId;
        this.phoneKey = phoneKey;
    }

    public Long getExistingVendorId() {
        return existingVendorId;
    }

    public String getPhoneKey() {
        return phoneKey;
    }
}
