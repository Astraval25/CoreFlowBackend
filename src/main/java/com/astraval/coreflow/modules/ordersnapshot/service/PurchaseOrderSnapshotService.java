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
import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.items.repo.ItemRepository;
import com.astraval.coreflow.modules.ordersnapshot.OrderSnapshot;
import com.astraval.coreflow.modules.ordersnapshot.dto.CreatePurchaseOrder;
import com.astraval.coreflow.modules.ordersnapshot.dto.PurchaseOrderSummaryDto;
import com.astraval.coreflow.modules.ordersnapshot.mapper.OrderSnapshotMapper;
import com.astraval.coreflow.modules.ordersnapshot.repo.PurchaseOrderSnapshotRepository;
import com.astraval.coreflow.modules.orderitemsnapshot.OrderItemSnapshot;
import com.astraval.coreflow.modules.orderitemsnapshot.OrderItemSnapshotService;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssets;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssetsRepository;
import com.astraval.coreflow.modules.vendor.VendorRepository;
import com.astraval.coreflow.modules.ordersnapshot.dto.UpdatePurchaseOrder;
import com.astraval.coreflow.modules.vendor.Vendors;

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
    private UserCompanyAssetsRepository userCompanyAssetsRepository;

  @Transactional
  public Long createPurchaseOrder(Long companyId, CreatePurchaseOrder createOrder) {
      
      // Access Validation
      Companies buyerCompany = companyRepository.findById(companyId)
              .orElseThrow(() -> new RuntimeException("Company not found"));
      
      // Get company assets from view
      UserCompanyAssets companyAssets = userCompanyAssetsRepository.findByCompanyId(companyId);
      if (companyAssets == null) {
          throw new RuntimeException("No assets found for company");
      }
      
      // Check vendor belongs to company
      if (companyAssets.getVendors() == null || !Arrays.asList(companyAssets.getVendors()).contains(createOrder.getVendorId())) {
          throw new RuntimeException("Vendor does not belong to the requesting company");
      }
      
      Vendors vendor = vendorRepository.findById(createOrder.getVendorId())
              .orElseThrow(() -> new RuntimeException("Vendor not found"));
      
      // Check items belong to company
      createOrder.getOrderItems().forEach(orderItem -> {
          if (companyAssets.getItems() == null || !Arrays.asList(companyAssets.getItems()).contains(orderItem.getItemId())) {
              throw new RuntimeException("Item does not belong to the requesting company: " + orderItem.getItemId());
          }
      });
      
      OrderSnapshot orderSnapshot = orderSnapshotMapper.toPurchaseOrderSnapshot(createOrder);
      orderSnapshot.setBuyerCompany(buyerCompany);
      orderSnapshot.setSellerCompany(vendor.getVendorCompany());
      orderSnapshot.setVendors(vendor);
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
          orderItem.setBasePrice(item.getPurchasePrice() != null ? item.getPurchasePrice() : item.getSalesPrice());
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

        UserCompanyAssets companyAssets = userCompanyAssetsRepository.findByCompanyId(companyId);
        if (companyAssets == null) {
            throw new RuntimeException("No assets found for company");
        }

        if (companyAssets.getVendors() == null
                || !Arrays.asList(companyAssets.getVendors()).contains(updateOrder.getVendorId())) {
            throw new RuntimeException("Vendor does not belong to the requesting company");
        }

        updateOrder.getOrderItems().forEach(orderItem -> {
            if (companyAssets.getItems() == null
                    || !Arrays.asList(companyAssets.getItems()).contains(orderItem.getItemId())) {
                throw new RuntimeException("Item does not belong to the requesting company: " + orderItem.getItemId());
            }
        });

        Vendors vendor = vendorRepository.findById(updateOrder.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // Update order Snapshot
        existingOrder.setVendors(vendor);
        existingOrder.setSellerCompany(vendor.getVendorCompany());
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
            orderItem.setBasePrice(item.getPurchasePrice() != null ? item.getPurchasePrice() : item.getSalesPrice());
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
