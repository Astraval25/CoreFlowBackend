package com.astraval.coreflow.main_modules.customer;

public class DuplicateCustomerPhoneException extends RuntimeException {

    private final Long existingCustomerId;
    private final String phoneKey;

    public DuplicateCustomerPhoneException(String message, Long existingCustomerId, String phoneKey) {
        super(message);
        this.existingCustomerId = existingCustomerId;
        this.phoneKey = phoneKey;
    }

    public Long getExistingCustomerId() {
        return existingCustomerId;
    }

    public String getPhoneKey() {
        return phoneKey;
    }
}
