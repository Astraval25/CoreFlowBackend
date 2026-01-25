package com.astraval.coreflow.modules.orderdetails.service;

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
import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.dto.CreatePurchaseOrder;
import com.astraval.coreflow.modules.orderdetails.dto.PurchaseOrderSummaryDto;
import com.astraval.coreflow.modules.orderdetails.mapper.OrderDetailsMapper;
import com.astraval.coreflow.modules.orderdetails.repo.PurchaseOrderDetailsRepository;
import com.astraval.coreflow.modules.orderdetails.service.OrderDetailsService;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetails;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetailsService;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssets;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssetsRepository;
import com.astraval.coreflow.modules.vendor.VendorRepository;
import com.astraval.coreflow.modules.orderdetails.dto.UpdatePurchaseOrder;
import com.astraval.coreflow.modules.vendor.VendorRepository;
import com.astraval.coreflow.modules.vendor.Vendors;

@Service
public class PurchaseOrderDetailsService {
  @Autowired
  private PurchaseOrderDetailsRepository purchaseOrderDetailsRepository;
  
  @Autowired
  private OrderDetailsService orderDetailsService;
  
  @Autowired
  private CompanyRepository companyRepository;
  
  @Autowired
  private ItemRepository itemRepository;
  
  @Autowired
  private OrderDetailsMapper orderDetailsMapper;
  
  @Autowired
  private OrderItemDetailsService orderItemDetailsService;
  
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
      
      OrderDetails orderDetails = orderDetailsMapper.toPurchaseOrderDetails(createOrder);
      orderDetails.setBuyerCompany(buyerCompany);
      orderDetails.setSellerCompany(vendor.getVendorCompany());
      orderDetails.setVendors(vendor);
      orderDetails.setOrderDate(LocalDateTime.now());
      orderDetails.setDeliveryCharge(createOrder.getDeliveryCharge());
      orderDetails.setDiscountAmount(createOrder.getDiscountAmount());
      orderDetails.setTaxAmount(createOrder.getTaxAmount());
      orderDetails.setHasBill(createOrder.isHasBill());
      
      String orderNumber = orderDetailsService.getNextSequenceNumber(companyId);
      orderDetails.setOrderNumber(orderNumber);
      
      OrderDetails savedOrder = purchaseOrderDetailsRepository.save(orderDetails);
      AtomicReference<Double> orderTotalAmount = new AtomicReference<>(0.0);
      
      createOrder.getOrderItems().forEach(newOrderItem -> {
          OrderItemDetails orderItem = new OrderItemDetails();
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
          orderItemDetailsService.createOrderItem(orderItem);
      });
      
      Double orderAmount = orderTotalAmount.get() + createOrder.getDeliveryCharge();
      Double totalAmount = orderAmount - createOrder.getDiscountAmount() + createOrder.getTaxAmount();
      savedOrder.setOrderAmount(orderAmount);
      savedOrder.setTotalAmount(totalAmount);
      purchaseOrderDetailsRepository.save(savedOrder);
      
      vendor.setDueAmount(vendor.getDueAmount() + totalAmount);
      vendorRepository.save(vendor);
      
      return savedOrder.getOrderId();
  }

  public List<PurchaseOrderSummaryDto> getOrderSummaryByCompanyId(Long companyId) {
    return purchaseOrderDetailsRepository.findPurchaseOrdersByCompanyId(companyId);
  }

  @Transactional
  public void updatePurchaseOrder(Long companyId, Long orderId, UpdatePurchaseOrder updateOrder) {
      // Validation
      companyRepository.findById(companyId)
              .orElseThrow(() -> new RuntimeException("Company not found"));
      
      OrderDetails existingOrder = purchaseOrderDetailsRepository.findById(orderId)
              .orElseThrow(() -> new RuntimeException("Order not found"));
      
      if (!existingOrder.getBuyerCompany().getCompanyId().equals(companyId)) {
          throw new RuntimeException("Order does not belong to the requesting company");
      }
      
      UserCompanyAssets companyAssets = userCompanyAssetsRepository.findByCompanyId(companyId);
      if (companyAssets == null) {
          throw new RuntimeException("No assets found for company");
      }
      
      if (companyAssets.getVendors() == null || !Arrays.asList(companyAssets.getVendors()).contains(updateOrder.getVendorId())) {
          throw new RuntimeException("Vendor does not belong to the requesting company");
      }
      
      updateOrder.getOrderItems().forEach(orderItem -> {
          if (companyAssets.getItems() == null || !Arrays.asList(companyAssets.getItems()).contains(orderItem.getItemId())) {
              throw new RuntimeException("Item does not belong to the requesting company: " + orderItem.getItemId());
          }
      });
      
      Vendors vendor = vendorRepository.findById(updateOrder.getVendorId())
              .orElseThrow(() -> new RuntimeException("Vendor not found"));
      
      // Update order details
      existingOrder.setVendors(vendor);
      existingOrder.setSellerCompany(vendor.getVendorCompany());
      existingOrder.setDeliveryCharge(updateOrder.getDeliveryCharge());
      existingOrder.setDiscountAmount(updateOrder.getDiscountAmount());
      existingOrder.setTaxAmount(updateOrder.getTaxAmount());
      existingOrder.setHasBill(updateOrder.isHasBill());
      
      // Delete existing order items
      orderItemDetailsService.deleteOrderItemsByOrderId(orderId);
      
      // Create new order items
      AtomicReference<Double> orderTotalAmount = new AtomicReference<>(0.0);
      updateOrder.getOrderItems().forEach(newOrderItem -> {
          OrderItemDetails orderItem = new OrderItemDetails();
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
          orderItemDetailsService.createOrderItem(orderItem);
      });
      
      // Update totals
      Double orderAmount = orderTotalAmount.get() + updateOrder.getDeliveryCharge();
      Double totalAmount = orderAmount - updateOrder.getDiscountAmount() + updateOrder.getTaxAmount();
      
      // Adjust vendor due amount
      vendor.setDueAmount(vendor.getDueAmount() - existingOrder.getTotalAmount() + totalAmount);
      vendorRepository.save(vendor);
      
      existingOrder.setOrderAmount(orderAmount);
      existingOrder.setTotalAmount(totalAmount);
      purchaseOrderDetailsRepository.save(existingOrder);
  }

}
