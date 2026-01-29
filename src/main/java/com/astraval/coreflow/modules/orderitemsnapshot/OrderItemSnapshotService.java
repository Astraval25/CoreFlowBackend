package com.astraval.coreflow.modules.orderitemsnapshot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.items.repo.ItemRepository;
import com.astraval.coreflow.modules.orderitemsnapshot.dto.CreateOrderItem;

@Service
public class OrderItemSnapshotService {

    @Autowired
    private OrderItemSnapshotRepository orderItemSnapshotRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderItemSnapshotMapper orderItemSnapshotMapper;

    @Transactional
    public OrderItemSnapshot createOrderItem(OrderItemSnapshot orderItemSnapshot) {
        return orderItemSnapshotRepository.save(orderItemSnapshot);
    }

    @Transactional
    public OrderItemSnapshot addOrderItem(Long orderId, CreateOrderItem createOrderItem) {
        Items item = itemRepository.findById(createOrderItem.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        OrderItemSnapshot orderItem = orderItemSnapshotMapper.toOrderItemSnapshot(createOrderItem);
        orderItem.setOrderId(orderId);
        orderItem.setItemId(item);
        orderItem.setBasePrice(item.getSalesPrice() != null ? item.getSalesPrice() : item.getPurchasePrice());
        orderItem.setItemTotal(createOrderItem.getQuantity() * createOrderItem.getUpdatedPrice());
        orderItem.setReadyStatus(0.0);

        return orderItemSnapshotRepository.save(orderItem);
    }

    @Transactional
    public void deleteOrderItemsByOrderId(Long orderId) {
        orderItemSnapshotRepository.deleteByOrderId(orderId);
    }
}
