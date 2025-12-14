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
    public VendorProjection createVendor(CreateVendorRequest request) {
        // Get current user's company
        String userIdStr = securityUtil.getCurrentSub();
        Integer userId = Integer.valueOf(userIdStr);
        
        // Get company from JWT claims
        Integer companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new RuntimeException("Company ID not found in token");
        }
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
    
    public List<VendorProjection> getAllVendors() {
        Integer companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new RuntimeException("Company ID not found in token");
        }
        
        return vendorRepository.findByCompanyCompanyIdAndIsActiveTrue(companyId)
            .stream()
            .map(vendorMapper::toProjection)
            .toList();
    }
}