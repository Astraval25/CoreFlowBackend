package com.astraval.coreflow.modules.orderdetails;

public class OrderStatus {
    public static final String NEW_ORDER = "NEW_ORDER";
    public static final String SALES_ORDER = "SALES_ORDER";
    
    public static String getNewOrder() {
        return NEW_ORDER;
    }
    
    public static String getSalesOrder() {
        return SALES_ORDER;
    }
}