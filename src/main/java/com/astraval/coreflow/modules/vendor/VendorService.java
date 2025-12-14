package com.astraval.coreflow.modules.vendor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.address.AddressRepository;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompaniesRepository;
import com.astraval.coreflow.global.util.SecurityUtil;
import com.astraval.coreflow.modules.vendor.dto.CreateVendorRequest;
import com.astraval.coreflow.modules.vendor.dto.UpdateVendorRequest;
import com.astraval.coreflow.modules.vendor.projection.VendorProjection;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private CompaniesRepository companiesRepository;
    
    @Autowired
    private VendorMapper vendorMapper;
    
    @Autowired
    private SecurityUtil securityUtil;
    
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
            billingAddress = addressRepository.save(billingAddress);
            vendor.setBillingAddrId(billingAddress.getAddressId().toString());
        }
        
        // Create shipping address if provided
        if (request.getShippingAddress() != null && request.getShippingAddress().getLine1() != null) {
            Address shippingAddress = vendorMapper.toAddress(request.getShippingAddress());
            shippingAddress.setIsActive(true);
            shippingAddress.setCreatedBy(userIdStr);
            shippingAddress.setCreatedDt(LocalDateTime.now());
            shippingAddress = addressRepository.save(shippingAddress);
            vendor.setShippingAddrId(shippingAddress.getAddressId().toString());
        } else if (Boolean.TRUE.equals(request.getSameForShipping()) && vendor.getBillingAddrId() != null) {
            // Use billing address for shipping if sameForShipping is true
            vendor.setShippingAddrId(vendor.getBillingAddrId());
        }
        
        vendor = vendorRepository.save(vendor);
        return vendorMapper.toProjection(vendor);
    }
    
    public List<VendorProjection> getAllVendors(Integer companyId) {
        
        return vendorRepository.findByCompanyCompanyIdAndIsActiveTrue(companyId)
            .stream()
            .map(vendorMapper::toProjection)
            .toList();
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
        return vendorMapper.toProjection(vendor);
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
}