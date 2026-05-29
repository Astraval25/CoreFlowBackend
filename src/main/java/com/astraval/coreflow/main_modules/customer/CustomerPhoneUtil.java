package com.astraval.coreflow.main_modules.customer;

public final class CustomerPhoneUtil {

    private CustomerPhoneUtil() {
    }

    public static String sanitizeToDigits(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }
        return phone.replaceAll("[^0-9]", "");
    }

    public static String toLast10PhoneKey(String phone) {
        String digits = sanitizeToDigits(phone);
        if (digits.length() < 10) {
            return null;
        }
        return digits.substring(digits.length() - 10);
    }
}
