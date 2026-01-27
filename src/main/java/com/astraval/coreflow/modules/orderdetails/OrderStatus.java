package com.astraval.coreflow.modules.orderdetails;

public class OrderStatus {
    public static final String NEW_ORDER = "NEW_ORDER";
    public static final String ORDER = "ORDER";
    public static final String VIEWED = "VIEWED";
    
    public static String getNewOrder() {
        return NEW_ORDER;
    }
    
    public static String getOrder() {
        return ORDER;
    }
    
    public static String getViewed() {
        return VIEWED;
    }
}