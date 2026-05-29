package com.astraval.coreflow.main_modules.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CustomerPhoneUtilTest {

    @Test
    void sanitizeToDigits_removesNonDigitCharacters() {
        assertEquals("919876543210", CustomerPhoneUtil.sanitizeToDigits("+91 98765-43210"));
        assertEquals("", CustomerPhoneUtil.sanitizeToDigits("() -"));
        assertEquals("", CustomerPhoneUtil.sanitizeToDigits(null));
    }

    @Test
    void toLast10PhoneKey_returnsNullForShortNumbers() {
        assertNull(CustomerPhoneUtil.toLast10PhoneKey("987654321"));
        assertNull(CustomerPhoneUtil.toLast10PhoneKey(null));
    }

    @Test
    void toLast10PhoneKey_usesLast10Digits() {
        assertEquals("9876543210", CustomerPhoneUtil.toLast10PhoneKey("+91 98765 43210"));
        assertEquals("1234567890", CustomerPhoneUtil.toLast10PhoneKey("001234567890"));
    }
}
