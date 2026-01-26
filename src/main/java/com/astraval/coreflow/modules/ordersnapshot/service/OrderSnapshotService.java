package com.astraval.coreflow.modules.ordersnapshot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.ordersnapshot.OrderSnapshot;
import com.astraval.coreflow.modules.ordersnapshot.repo.OrderSnapshotRepository;

@Service
public class OrderSnapshotService {

    @Autowired
    private OrderSnapshotRepository orderSnapshotRepository;

    public OrderSnapshot getOrderSnapshotByOrderId(Long companyId, Long orderId) {
        return orderSnapshotRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
    public void deleteOrder(Long companyId, Long orderId) {
        OrderSnapshot order = orderSnapshotRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        orderSnapshotRepository.delete(order);
    }

    @Transactional
    public void deactivateOrder(Long companyId, Long orderId) {
        OrderSnapshot order = orderSnapshotRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setIsActive(false);
        orderSnapshotRepository.save(order);
    }

    @Transactional
    public void activateOrder(Long companyId, Long orderId) {
        OrderSnapshot order = orderSnapshotRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setIsActive(true);
        orderSnapshotRepository.save(order);
    }

    // -----------------------> Helper functions
    public String getNextSequenceNumber(Long companyId) {
        return orderSnapshotRepository.generateOrderNumber(companyId);
    }
}
