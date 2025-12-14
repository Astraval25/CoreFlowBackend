package com.astraval.coreflow.modules.vendor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompaniesRepository;
import com.astraval.coreflow.global.util.SecurityUtil;
import com.astraval.coreflow.modules.vendor.dto.CreateVendorRequest;
import com.astraval.coreflow.modules.vendor.dto.UpdateVendorRequest;
import com.astraval.coreflow.modules.vendor.projection.VendorProjection;
import com.astraval.coreflow.modules.address.facade.AddressFacade;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;
    

    
    @Autowired
    private CompaniesRepository companiesRepository;
    
    @Autowired
    private VendorMapper vendorMapper;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    @Autowired
    private AddressFacade addressFacade;

    @Transactional
    public VendorProjection createVendor(Integer companyId, CreateVendorRequest request) {
        // Get current user info
        String userIdStr = securityUtil.getCurrentSub();
        Integer userId = Integer.valueOf(userIdStr);
        
        // Get company by provided companyId
        Companies company = companiesRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
        
        // Create vendor
        Vendors vendor = vendorMapper.toVendor(request);
        vendor.setCompany(company);
        vendor.setIsActive(true);
        vendor.setCreatedAt(LocalDateTime.now());
        vendor.setCreatedBy(Long.valueOf(userId));
        
        // Create billing address if provided
        if (request.getBillingAddress() != null && request.getBillingAddress().getLine1() != null) {
            Address billingAddress = vendorMapper.toAddress(request.getBillingAddress());
            billingAddress.setIsActive(true);
            billingAddress.setCreatedBy(userIdStr);
            billingAddress.setCreatedDt(LocalDateTime.now());
            billingAddress = addressFacade.createAddress(billingAddress);
            vendor.setBillingAddrId(billingAddress.getAddressId().toString());
        }
        
        // Create shipping address if provided
        if (request.getShippingAddress() != null && request.getShippingAddress().getLine1() != null) {
            Address shippingAddress = vendorMapper.toAddress(request.getShippingAddress());
            shippingAddress.setIsActive(true);
            shippingAddress.setCreatedBy(userIdStr);
            shippingAddress.setCreatedDt(LocalDateTime.now());
            shippingAddress = addressFacade.createAddress(shippingAddress);
            vendor.setShippingAddrId(shippingAddress.getAddressId().toString());
        } else if (Boolean.TRUE.equals(request.getSameForShipping()) && vendor.getBillingAddrId() != null) {
            // Use billing address for shipping if sameForShipping is true
            vendor.setShippingAddrId(vendor.getBillingAddrId());
        }
        
        vendor = vendorRepository.save(vendor);
        return mapVendorWithAddresses(vendor);
    }
    
    public List<VendorProjection> getAllVendors(Integer companyId) {
        
        return vendorRepository.findByCompanyCompanyIdAndIsActiveTrue(companyId)
            .stream()
            .map(this::mapVendorWithAddresses)
            .toList();
    }
    
    public VendorProjection getVendorById(Integer companyId, Long vendorId) {
        Vendors vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
            
        if (!vendor.getCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Vendor does not belong to the specified company");
        }
        
        if (!vendor.getIsActive()) {
            throw new RuntimeException("Vendor is not active");
        }
        
        return mapVendorWithAddresses(vendor);
    }
    
    @Transactional
    public VendorProjection updateVendor(Integer companyId, Long vendorId, UpdateVendorRequest request) {
        String userIdStr = securityUtil.getCurrentSub();
        
        Vendors vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
            
        if (!vendor.getCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Vendor does not belong to your company");
        }
        
        // Update vendor fields
        vendor.setVendorName(request.getVendorName());
        vendor.setDisplayName(request.getDisplayName());
        vendor.setEmail(request.getEmail());
        vendor.setPhone(request.getPhone());
        vendor.setLang(request.getLang());
        vendor.setPan(request.getPan());
        vendor.setGst(request.getGst());
        vendor.setAdvanceAmount(request.getAdvanceAmount());
        vendor.setUpdatedAt(LocalDateTime.now());
        vendor.setUpdateAt(Long.valueOf(userIdStr));
        
        vendor = vendorRepository.save(vendor);
        return mapVendorWithAddresses(vendor);
    }
    
    @Transactional
    public void deactivateVendor(Integer companyId, Long vendorId) {
        String userIdStr = securityUtil.getCurrentSub();
        
        Vendors vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
            
        if (!vendor.getCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Vendor does not belong to your company");
        }
        
        vendor.setIsActive(false);
        vendor.setUpdatedAt(LocalDateTime.now());
        vendor.setUpdateAt(Long.valueOf(userIdStr));
        
        vendorRepository.save(vendor);
    }
    
    private VendorProjection mapVendorWithAddresses(Vendors vendor) {
        VendorProjection projection = vendorMapper.toProjection(vendor);
        
        // Load billing address if exists
        if (vendor.getBillingAddrId() != null) {
            try {
                Integer billingAddrId = Integer.valueOf(vendor.getBillingAddrId());
                projection.setBillingAddress(addressFacade.getAddressById(billingAddrId));
            } catch (NumberFormatException e) {
                log.warn("Invalid billing address ID format for vendor {}", vendor.getVendorId());
            } catch (Exception e) {
                log.warn("Failed to load billing address for vendor {}", vendor.getVendorId());
            }
        }
        
        // Load shipping address if exists
        if (vendor.getShippingAddrId() != null) {
            try {
                Integer shippingAddrId = Integer.valueOf(vendor.getShippingAddrId());
                projection.setShippingAddress(addressFacade.getAddressById(shippingAddrId));
            } catch (NumberFormatException e) {
                log.warn("Invalid shipping address ID format for vendor {}", vendor.getVendorId());
            } catch (Exception e) {
                log.warn("Failed to load shipping address for vendor {}", vendor.getVendorId());
            }
        }
        
        return projection;
    }
}