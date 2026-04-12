package com.astraval.coreflow.main_modules.ordersnapshot.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.customer.CustomerService;
import com.astraval.coreflow.main_modules.customer.Customers;
import com.astraval.coreflow.main_modules.items.model.Items;
import com.astraval.coreflow.main_modules.items.repo.ItemRepository;
import com.astraval.coreflow.main_modules.orderitemsnapshot.OrderItemSnapshot;
import com.astraval.coreflow.main_modules.orderitemsnapshot.OrderItemSnapshotService;
import com.astraval.coreflow.main_modules.ordersnapshot.OrderSnapshot;
import com.astraval.coreflow.main_modules.ordersnapshot.dto.CreatePurchaseOrder;
import com.astraval.coreflow.main_modules.ordersnapshot.dto.PurchaseOrderSummaryDto;
import com.astraval.coreflow.main_modules.ordersnapshot.dto.UpdatePurchaseOrder;
import com.astraval.coreflow.main_modules.ordersnapshot.mapper.OrderSnapshotMapper;
import com.astraval.coreflow.main_modules.ordersnapshot.repo.PurchaseOrderSnapshotRepository;
import com.astraval.coreflow.main_modules.vendor.VendorRepository;
import com.astraval.coreflow.main_modules.vendor.Vendors;

@Service
public class PurchaseOrderSnapshotService {
    @Autowired
    private PurchaseOrderSnapshotRepository purchaseOrderSnapshotRepository;

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
    private VendorRepository vendorRepository;

    @Autowired
    private CustomerService customerService;

  @Transactional
  public Long createPurchaseOrder(Long companyId, CreatePurchaseOrder createOrder) {
      
      // Access Validation
      companyRepository.findById(companyId)
              .orElseThrow(() -> new RuntimeException("Company not found"));

      Vendors vendor = vendorRepository.findById(createOrder.getVendorId())
              .orElseThrow(() -> new RuntimeException("Vendor not found"));

      OrderSnapshot orderSnapshot = orderSnapshotMapper.toPurchaseOrderSnapshot(createOrder);
      orderSnapshot.setVendors(vendor);
      if (vendor.getVendorCompany() != null) {
          Long vendorsCustomerCompanyId = vendor.getVendorCompany().getCompanyId();
          Customers sellerCustomer = customerService.getSellersCustomerId(vendorsCustomerCompanyId, companyId);
          orderSnapshot.setCustomers(sellerCustomer);
      }
      orderSnapshot.setOrderDate(LocalDateTime.now());
      orderSnapshot.setDeliveryCharge(createOrder.getDeliveryCharge());
      orderSnapshot.setDiscountAmount(createOrder.getDiscountAmount());
      orderSnapshot.setTaxAmount(createOrder.getTaxAmount());
      orderSnapshot.setHasBill(createOrder.isHasBill());
      
      String orderNumber = orderSnapshotService.getNextSequenceNumber(companyId);
      orderSnapshot.setOrderNumber(orderNumber);
      
      OrderSnapshot savedOrder = purchaseOrderSnapshotRepository.save(orderSnapshot);
      AtomicReference<Double> orderTotalAmount = new AtomicReference<>(0.0);
      
      createOrder.getOrderItems().forEach(newOrderItem -> {
          OrderItemSnapshot orderItem = new OrderItemSnapshot();
          Items item = itemRepository.findById(newOrderItem.getItemId())
                  .orElseThrow(() -> new RuntimeException("Item not found"));

          orderItem.setOrderId(savedOrder.getOrderId());
          orderItem.setItemId(item);
          orderItem.setQuantity(newOrderItem.getQuantity());
          orderItem.setBasePrice(item.getBasePurchasePrice() != null ? item.getBasePurchasePrice() : item.getBaseSalesPrice());
          orderItem.setUpdatedPrice(newOrderItem.getUpdatedPrice());
          orderItem.setItemTotal(newOrderItem.getQuantity() * newOrderItem.getUpdatedPrice());
          orderItem.setReadyStatus(0.0);
          orderItem.setStatus(null);
          orderTotalAmount.updateAndGet(current -> current + orderItem.getItemTotal());
          orderItemSnapshotService.createOrderItem(orderItem);
      });
      
      Double orderAmount = orderTotalAmount.get() + createOrder.getDeliveryCharge();
      Double totalAmount = orderAmount - createOrder.getDiscountAmount() + createOrder.getTaxAmount();
      savedOrder.setOrderAmount(orderAmount);
      savedOrder.setTotalAmount(totalAmount);
      purchaseOrderSnapshotRepository.save(savedOrder);
      
      return savedOrder.getOrderId();
  }

    public List<PurchaseOrderSummaryDto> getOrderSummaryByCompanyId(Long companyId) {
        return purchaseOrderSnapshotRepository.findPurchaseOrdersByCompanyId(companyId);
    }

    @Transactional
    public void updatePurchaseOrder(Long companyId, Long orderId, UpdatePurchaseOrder updateOrder) {
        // Validation
        companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        OrderSnapshot existingOrder = purchaseOrderSnapshotRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!existingOrder.getBuyerCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Order does not belong to the requesting company");
        }

        Vendors vendor = vendorRepository.findById(updateOrder.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // Update order Snapshot
        existingOrder.setVendors(vendor);
        if (vendor.getVendorCompany() != null) {
            Long vendorsCustomerCompanyId = vendor.getVendorCompany().getCompanyId();
            Customers sellerCustomer = customerService.getSellersCustomerId(vendorsCustomerCompanyId, companyId);
            existingOrder.setCustomers(sellerCustomer);
        } else {
            existingOrder.setCustomers(null);
        }
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
            orderItem.setBasePrice(item.getBasePurchasePrice() != null ? item.getBasePurchasePrice() : item.getBaseSalesPrice());
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
        purchaseOrderSnapshotRepository.save(existingOrder);
    }

}
