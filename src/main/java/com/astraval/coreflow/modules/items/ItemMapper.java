package com.astraval.coreflow.modules.items;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.items.dto.CreateItemDto;
import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.vendor.VendorRepository;
import com.astraval.coreflow.modules.vendor.Vendors;

@Component
public class ItemMapper {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    public void mapDtoToEntity(CreateItemDto dto, Items item) {
        item.setItemName(dto.getItemName());
        item.setItemType(dto.getItemType());
        item.setUnit(dto.getUnit());
        item.setSalesDescription(dto.getSalesDescription());
        item.setSalesPrice(dto.getSalesPrice());
        item.setPurchaseDescription(dto.getPurchaseDescription());
        item.setPurchasePrice(dto.getPurchasePrice());
        item.setHsnCode(dto.getHsnCode());
        item.setTaxRate(dto.getTaxRate());

        if (dto.getPreferredCustomerId() != null) {
            Customers customer = customerRepository.findById(dto.getPreferredCustomerId())
                    .orElseThrow(() -> new RuntimeException("Preferred customer not found with ID: " + dto.getPreferredCustomerId()));
            if (customer.getCompany().getCompanyId() != item.getCompany().getCompanyId()) {
                throw new RuntimeException("Customer does not belong to the same company");
            }
            item.setPreferredCustomer(customer);
        }

        if (dto.getPreferredVendorId() != null) {
            Vendors vendor = vendorRepository.findById(dto.getPreferredVendorId())
                    .orElseThrow(() -> new RuntimeException("Preferred vendor not found with ID: " + dto.getPreferredVendorId()));
            if (vendor.getCompany().getCompanyId() != item.getCompany().getCompanyId()) {
                throw new RuntimeException("Vendor does not belong to the same company");
            }
            item.setPreferredVendor(vendor);
        }
    }

    public void mapUpdateDtoToEntity(com.astraval.coreflow.modules.items.dto.UpdateItemDto dto, Items item) {
        if (dto.getItemName() != null) item.setItemName(dto.getItemName());
        if (dto.getItemType() != null) item.setItemType(dto.getItemType());
        if (dto.getUnit() != null) item.setUnit(dto.getUnit());
        if (dto.getSalesDescription() != null) item.setSalesDescription(dto.getSalesDescription());
        if (dto.getSalesPrice() != null) item.setSalesPrice(dto.getSalesPrice());
        if (dto.getPurchaseDescription() != null) item.setPurchaseDescription(dto.getPurchaseDescription());
        if (dto.getPurchasePrice() != null) item.setPurchasePrice(dto.getPurchasePrice());
        if (dto.getHsnCode() != null) item.setHsnCode(dto.getHsnCode());
        if (dto.getTaxRate() != null) item.setTaxRate(dto.getTaxRate());

        // Handle preferred customer - check if field is present in request
        if (hasField(dto, "preferredCustomerId")) {
            if (dto.getPreferredCustomerId() != null) {
                Customers customer = customerRepository.findById(dto.getPreferredCustomerId())
                        .orElseThrow(() -> new RuntimeException("Preferred customer not found with ID: " + dto.getPreferredCustomerId()));
                if (!customer.getCompany().getCompanyId().equals(item.getCompany().getCompanyId())) {
                    throw new RuntimeException("Customer does not belong to the same company");
                }
                item.setPreferredCustomer(customer);
            } else {
                item.setPreferredCustomer(null);
            }
        }

        // Handle preferred vendor - check if field is present in request
        if (hasField(dto, "preferredVendorId")) {
            if (dto.getPreferredVendorId() != null) {
                Vendors vendor = vendorRepository.findById(dto.getPreferredVendorId())
                        .orElseThrow(() -> new RuntimeException("Preferred vendor not found with ID: " + dto.getPreferredVendorId()));
                if (!vendor.getCompany().getCompanyId().equals(item.getCompany().getCompanyId())) {
                    throw new RuntimeException("Vendor does not belong to the same company");
                }
                item.setPreferredVendor(vendor);
            } else {
                item.setPreferredVendor(null);
            }
        }
    }

    private boolean hasField(Object dto, String fieldName) {
        try {
            java.lang.reflect.Field field = dto.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}