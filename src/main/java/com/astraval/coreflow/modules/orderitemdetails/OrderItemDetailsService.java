package com.astraval.coreflow.modules.orderitemdetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.items.ItemRepository;
import com.astraval.coreflow.modules.items.Items;
import com.astraval.coreflow.modules.orderitemdetails.dto.CreateOrderItem;

@Service
public class OrderItemDetailsService {
  
    @Autowired
    private OrderItemDetailsRepository orderItemDetailsRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private OrderItemDetailsMapper orderItemDetailsMapper;
    
    @Transactional
    public OrderItemDetails createOrderItem(OrderItemDetails orderItemDetails) {
        return orderItemDetailsRepository.save(orderItemDetails);
    }
    
    @Transactional
    public OrderItemDetails addOrderItem(Long orderId, CreateOrderItem createOrderItem) {
        Items item = itemRepository.findById(createOrderItem.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        OrderItemDetails orderItem = orderItemDetailsMapper.toOrderItemDetails(createOrderItem);
        orderItem.setOrderId(orderId);
        orderItem.setItemId(item);
        orderItem.setBasePrice(item.getSalesPrice() != null ? item.getSalesPrice() : item.getPurchasePrice());
        orderItem.setItemTotal(createOrderItem.getQuantity() * createOrderItem.getUpdatedPrice());
        orderItem.setReadyStatus(0.0);
        
        return orderItemDetailsRepository.save(orderItem);
    }
}
