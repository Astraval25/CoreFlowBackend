package com.astraval.coreflow.modules.payments;

public class PaymentStatus {
    // Payment Status
    public static final String PAID = "PAID";
    public static final String PAYMENT_VIEWED = "PAYMENT_VIEWED";
    public static final String PAYMENT_ACCEPTED = "PAYMENT_ACCEPTED";
    public static final String PAYMENT_DECLINED = "PAYMENT_DECLINED";
    
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
    
}