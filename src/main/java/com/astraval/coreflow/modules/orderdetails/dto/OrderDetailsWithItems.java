package com.astraval.coreflow.modules.orderdetails.dto;

import java.util.List;

import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsWithItems {
    private OrderDetails orderDetails;
    private List<OrderItemDetails> orderItems;
}