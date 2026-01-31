package com.astraval.coreflow.modules.orderdetails;

public class OrderStatus {
    // Quotation Status
    public static final String QUOTATION = "QUOTATION";
    public static final String QUOTATION_VIEWED = "QUOTATION_VIEWED";
    public static final String QUOTATION_ACCEPTED = "QUOTATION_ACCEPTED";
    public static final String QUOTATION_DECLINED = "QUOTATION_DECLINED";
    
    // Sales/Purchase Order Status
    public static final String ORDER_VIEWED = "ORDER_VIEWED";
    public static final String ORDER = "ORDER";
    
    // Invoice Status
    public static final String ORDER_INVOICED = "ORDER_INVOICED";
    
    // Payment Status
    public static final String ORDER_PAYED = "ORDER_PAYED";
    

    public static String getQuotation() {       // Action Status
        return QUOTATION;
    }
    
    public static String getQuotationViewed() {         // info Status
        return QUOTATION_VIEWED;
    }
    
    public static String getQuotationAccepted() {       // info Status
        return QUOTATION_ACCEPTED;
    }
    
    public static String getQuotationDecline(){         // info Status
        return QUOTATION_DECLINED;
    }
    
    public static String getOrder() {           // Action Status
        return ORDER;
    }
    
    public static String getOrderViewed() {             // info Status
        return ORDER_VIEWED;
    }
    
    public static String getOrderInvoiced() {   // Action Status
        return ORDER_INVOICED;
    }
    
    public static String getOrderPayed() {      // Action Status
        return ORDER_PAYED;
    }
}