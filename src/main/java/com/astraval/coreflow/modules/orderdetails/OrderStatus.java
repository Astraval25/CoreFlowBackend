package com.astraval.coreflow.modules.orderdetails;

public class OrderStatus {
    public static final String NEW_ORDER = "NEW_ORDER";
    public static final String ORDER_VIEWED = "ORDER_VIEWED";
    public static final String ORDER = "ORDER";
    public static final String ORDER_INVOICED = "ORDER_INVOICED";
    public static final String ORDER_PAYED = "ORDER_PAYED";
    
    public static String getNewOrder() {
        return NEW_ORDER;
    }
    
    public static String getOrder() {
        return ORDER;
    }
    
    public static String getOrderViewed() {
        return ORDER_VIEWED;
    }
}