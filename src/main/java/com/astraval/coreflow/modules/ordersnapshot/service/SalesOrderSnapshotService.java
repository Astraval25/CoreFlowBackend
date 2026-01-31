package com.astraval.coreflow.modules.ordersnapshot.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.items.repo.ItemRepository;
import com.astraval.coreflow.modules.ordersnapshot.OrderSnapshot;
import com.astraval.coreflow.modules.ordersnapshot.dto.CreateSalesOrder;
import com.astraval.coreflow.modules.ordersnapshot.dto.SalesOrderSummaryDto;
import com.astraval.coreflow.modules.ordersnapshot.mapper.OrderSnapshotMapper;
import com.astraval.coreflow.modules.ordersnapshot.repo.SalesOrderSnapshotRepository;
import com.astraval.coreflow.modules.orderitemsnapshot.OrderItemSnapshot;
import com.astraval.coreflow.modules.orderitemsnapshot.OrderItemSnapshotService;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssets;
import com.astraval.coreflow.modules.ordersnapshot.dto.UpdateSalesOrder;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssetsRepository;

@Service
public class SalesOrderSnapshotService {

    @Autowired
    private SalesOrderSnapshotRepository salesOrderSnapshotRepository;

    @Autowired
    private OrderSnapshotService orderSnapshotService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderSnapshotMapper orderSnapshotMapper;

    @Autowired
    private OrderItemSnapshotService orderItemSnapshotService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserCompanyAssetsRepository userCompanyAssetsRepository;

    @Transactional
    public Long createSalesOrder(Long companyId, CreateSalesOrder createOrder) {

        // Access Validation
        // 1. check the companyId is exist
        Companies sellerCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Get company assets from view
        UserCompanyAssets companyAssets = userCompanyAssetsRepository.findByCompanyId(companyId);
        if (companyAssets == null) {
            throw new RuntimeException("No assets found for company");
        }

        // 2. check the customerId exists and belongs to the requesting company
        if (companyAssets.getCustomers() == null
                || !Arrays.asList(companyAssets.getCustomers()).contains(createOrder.getCustomerId())) {
            throw new RuntimeException("Customer does not belong to the requesting company");
        }

        Customers toCustomers = customerRepository.findById(createOrder.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // 3. check the orderItems.items exist and belong to the requesting company
        createOrder.getOrderItems().forEach(orderItem -> {
            if (companyAssets.getItems() == null
                    || !Arrays.asList(companyAssets.getItems()).contains(orderItem.getItemId())) {
                throw new RuntimeException("Item does not belong to the requesting company: " + orderItem.getItemId());
            }
        });
        // Access Validation Done if all ok then only allow to create.

        OrderSnapshot orderSnapshot = orderSnapshotMapper.toOrderSnapshot(createOrder);
        orderSnapshot.setSellerCompany(sellerCompany);
        orderSnapshot.setBuyerCompany(toCustomers.getCustomerCompany() != null ? toCustomers.getCustomerCompany() : sellerCompany);
        orderSnapshot.setCustomers(toCustomers);
        orderSnapshot.setOrderDate(LocalDateTime.now());
        orderSnapshot.setDeliveryCharge(createOrder.getDeliveryCharge());
        orderSnapshot.setDiscountAmount(createOrder.getDiscountAmount());
        orderSnapshot.setTaxAmount(createOrder.getTaxAmount());
        orderSnapshot.setHasBill(createOrder.isHasBill());

        // Generate order number
        String orderNumber = orderSnapshotService.getNextSequenceNumber(companyId);
        orderSnapshot.setOrderNumber(orderNumber);

        OrderSnapshot savedOrder = salesOrderSnapshotRepository.save(orderSnapshot);
        AtomicReference<Double> orderTotalAmount = new AtomicReference<>(0.0);
        // Create order items
        createOrder.getOrderItems().forEach(newOrderItem -> {
            OrderItemSnapshot orderItem = new OrderItemSnapshot();
            Items item = itemRepository.findById(newOrderItem.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            orderItem.setOrderId(savedOrder.getOrderId());
            orderItem.setItemId(item);
            orderItem.setQuantity(newOrderItem.getQuantity());
            orderItem.setBasePrice(item.getSalesPrice() != null ? item.getSalesPrice() : item.getPurchasePrice());
            orderItem.setUpdatedPrice(newOrderItem.getUpdatedPrice());
            orderItem.setItemTotal(newOrderItem.getQuantity() * newOrderItem.getUpdatedPrice());
            orderItem.setReadyStatus(0.0);
            orderItem.setStatus(null); // as of now no need for item level status tracking
            orderTotalAmount.updateAndGet(current -> current + orderItem.getItemTotal());
            orderItemSnapshotService.createOrderItem(orderItem);
        });

        // Update order total amount
        Double orderAmount = orderTotalAmount.get() + createOrder.getDeliveryCharge();
        Double totalAmount = orderAmount - createOrder.getDiscountAmount() + createOrder.getTaxAmount();
        savedOrder.setOrderAmount(orderAmount);
        savedOrder.setTotalAmount(totalAmount);
        salesOrderSnapshotRepository.save(savedOrder);

        return savedOrder.getOrderId();
    }

    public List<SalesOrderSummaryDto> getOrderSummaryByCompanyId(Long companyId) {
        return salesOrderSnapshotRepository.findOrdersByCompanyId(companyId);
    }

    @Transactional
    public void updateSalesOrder(Long companyId, Long orderId, UpdateSalesOrder updateOrder) {
        // Validation
        companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        OrderSnapshot existingOrder = salesOrderSnapshotRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!existingOrder.getSellerCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Order does not belong to the requesting company");
        }

        UserCompanyAssets companyAssets = userCompanyAssetsRepository.findByCompanyId(companyId);
        if (companyAssets == null) {
            throw new RuntimeException("No assets found for company");
        }

        if (companyAssets.getCustomers() == null
                || !Arrays.asList(companyAssets.getCustomers()).contains(updateOrder.getCustomerId())) {
            throw new RuntimeException("Customer does not belong to the requesting company");
        }

        updateOrder.getOrderItems().forEach(orderItem -> {
            if (companyAssets.getItems() == null
                    || !Arrays.asList(companyAssets.getItems()).contains(orderItem.getItemId())) {
                throw new RuntimeException("Item does not belong to the requesting company: " + orderItem.getItemId());
            }
        });

        Customers toCustomers = customerRepository.findById(updateOrder.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Update order Snapshot
        existingOrder.setCustomers(toCustomers);
        existingOrder.setBuyerCompany(toCustomers.getCustomerCompany() != null ? toCustomers.getCustomerCompany()
                : existingOrder.getSellerCompany());
        existingOrder.setDeliveryCharge(updateOrder.getDeliveryCharge());
        existingOrder.setDiscountAmount(updateOrder.getDiscountAmount());
        existingOrder.setTaxAmount(updateOrder.getTaxAmount());
        existingOrder.setHasBill(updateOrder.isHasBill());

        // Delete existing order items
        orderItemSnapshotService.deleteOrderItemsByOrderId(orderId);

        // Create new order items
        AtomicReference<Double> orderTotalAmount = new AtomicReference<>(0.0);
        updateOrder.getOrderItems().forEach(newOrderItem -> {
            OrderItemSnapshot orderItem = new OrderItemSnapshot();
            Items item = itemRepository.findById(newOrderItem.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            orderItem.setOrderId(existingOrder.getOrderId());
            orderItem.setItemId(item);
            orderItem.setQuantity(newOrderItem.getQuantity());
            orderItem.setBasePrice(item.getSalesPrice() != null ? item.getSalesPrice() : item.getPurchasePrice());
            orderItem.setUpdatedPrice(newOrderItem.getUpdatedPrice());
            orderItem.setItemTotal(newOrderItem.getQuantity() * newOrderItem.getUpdatedPrice());
            orderItem.setReadyStatus(0.0);
            orderItem.setStatus(null);
            orderTotalAmount.updateAndGet(current -> current + orderItem.getItemTotal());
            orderItemSnapshotService.createOrderItem(orderItem);
        });

        // Update totals
        Double orderAmount = orderTotalAmount.get() + updateOrder.getDeliveryCharge();
        Double totalAmount = orderAmount - updateOrder.getDiscountAmount() + updateOrder.getTaxAmount();

        existingOrder.setOrderAmount(orderAmount);
        existingOrder.setTotalAmount(totalAmount);
        salesOrderSnapshotRepository.save(existingOrder);
    }

}
