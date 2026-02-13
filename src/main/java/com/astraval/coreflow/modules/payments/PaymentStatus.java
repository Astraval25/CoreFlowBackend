package com.astraval.coreflow.modules.payments;

import java.util.Set;

public class PaymentStatus {
    // Payment Status
    public static final String PAID = "PAID";
    public static final String PAYMENT_VIEWED = "PAYMENT_VIEWED";
    public static final String PAYMENT_ACCEPTED = "PAYMENT_ACCEPTED";
    public static final String PAYMENT_DECLINED = "PAYMENT_DECLINED";
    public static final String PAYMENT_REFUND = "PAYMENT_REFUND";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String PARTIALLY_PAID = "PARTIALLY_PAID";

    private static final Set<String> SUPPORTED_STATUSES = Set.of(
            PAID,
            PAYMENT_VIEWED,
            PAYMENT_ACCEPTED,
            PAYMENT_DECLINED,
            PAYMENT_REFUND,
            PAYMENT_FAILED,
            PARTIALLY_PAID);
    
    public static String getPaid() { // Action Status
        return PAID;
    }
    
    public static String getPaymentViewed() {
        return PAYMENT_VIEWED;
    }
    
    public static String getPaymentAccepted() {
        return PAYMENT_ACCEPTED;
    }
    
    public static String getPaymentDeclined() {
        return PAYMENT_DECLINED;
    }

    public static String getPaymentRefund() {
        return PAYMENT_REFUND;
    }

    public static String getPaymentFailed() {
        return PAYMENT_FAILED;
    }

    public static String getPartiallyPaid() {
        return PARTIALLY_PAID;
    }

    public static boolean isSupportedStatus(String status) {
        return status != null && SUPPORTED_STATUSES.contains(status);
    }
    
}
