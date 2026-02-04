package com.astraval.coreflow.modules.vendor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.address.AddressMapper;
import com.astraval.coreflow.modules.address.AddressService;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.vendor.dto.CreateUpdateVendorDto;
import com.astraval.coreflow.modules.vendor.dto.VendorSummaryDto;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private AddressService addressService;
    
    @Autowired
    private AddressMapper addressMapper;


    @Transactional
    public Long createVendor(Long companyId, CreateUpdateVendorDto request) {
        try {
            Companies company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

            Vendors vendor = new Vendors();
            vendor.setCompany(company);
            vendor.setVendorName(request.getVendorName());
            vendor.setDisplayName(request.getDisplayName());
            vendor.setEmail(request.getEmail());
            vendor.setPhone(request.getPhone());
            vendor.setLang(request.getLang());
            vendor.setPan(request.getPan());
            vendor.setGst(request.getGst());
            vendor.setDueAmount(request.getDueAmount());
            vendor.setSameAsBillingAddress(request.isSameAsBillingAddress());

            // Create addresses if provided
            if (request.getBillingAddress() != null) {
                Address billingAddress = addressMapper.toAddress(request.getBillingAddress());
                Address savedBillingAddress = addressService.createAddress(billingAddress);
                vendor.setBillingAddrId(savedBillingAddress);
            }

            if (request.getShippingAddress() != null && !request.isSameAsBillingAddress()) {
                Address shippingAddress = addressMapper.toAddress(request.getShippingAddress());
                Address savedShippingAddress = addressService.createAddress(shippingAddress);
                vendor.setShippingAddrId(savedShippingAddress);
            } else if (request.isSameAsBillingAddress() && vendor.getBillingAddrId() != null) {
                vendor.setShippingAddrId(vendor.getBillingAddrId());
            }

            return vendorRepository.save(vendor).getVendorId();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create vendor: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updateVendor(Long companyId, Long vendorId, CreateUpdateVendorDto request) {
        try {
            Vendors vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

            vendor.setVendorName(request.getVendorName());
            vendor.setDisplayName(request.getDisplayName());
            vendor.setEmail(request.getEmail());
            vendor.setPhone(request.getPhone());
            vendor.setLang(request.getLang());
            vendor.setPan(request.getPan());
            vendor.setGst(request.getGst());
            vendor.setDueAmount(request.getDueAmount());
            vendor.setSameAsBillingAddress(request.isSameAsBillingAddress());

            // Update billing address
            if (request.getBillingAddress() != null) {
                if (vendor.getBillingAddrId() != null) {
                    addressService.updateAddress(vendor.getBillingAddrId().getAddressId(), 
                            addressMapper.toAddress(request.getBillingAddress()));
                } else {
                    Address billingAddress = addressService.createAddress(addressMapper.toAddress(request.getBillingAddress()));
                    vendor.setBillingAddrId(billingAddress);
                }
            }

            // Update shipping address
            if (request.isSameAsBillingAddress()) {
                vendor.setShippingAddrId(vendor.getBillingAddrId());
            } else if (request.getShippingAddress() != null) {
                if (vendor.getShippingAddrId() != null && !vendor.getShippingAddrId().equals(vendor.getBillingAddrId())) {
                    addressService.updateAddress(vendor.getShippingAddrId().getAddressId(), 
                            addressMapper.toAddress(request.getShippingAddress()));
                } else {
                    Address shippingAddress = addressService.createAddress(addressMapper.toAddress(request.getShippingAddress()));
                    vendor.setShippingAddrId(shippingAddress);
                }
            }

            vendorRepository.save(vendor);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update vendor: " + e.getMessage(), e);
        }
    }

    public List<VendorSummaryDto> getVendorsByCompany(Long companyId) {
        return vendorRepository.findByCompanyIdSummary(companyId);
    }

    public List<VendorSummaryDto> getActiveVendorsByCompany(Long companyId) {
        return vendorRepository.findByCompanyCompanyIdAndIsActiveOrderByDisplayName(companyId, true);
    }
    
    public List<VendorSummaryDto> getUnlinkedVendorsByCompany(Long companyId) {
        return vendorRepository.findUnlinkedByCompanyIdSummary(companyId);
    }

    public List<Vendors> getAllVendors() {
        return vendorRepository.findAll();
    }

    public Vendors getVendorById(Long companyId, Long vendorId) {
        Vendors vendor = vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));
        return vendor;
    }

    @Transactional
    public void deactivateVendor(Long vendorId) {
        Vendors vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));
        vendor.setIsActive(false);
        vendorRepository.save(vendor);
    }

    @Transactional
    public void activateVendor(Long vendorId) {
        Vendors vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));
        vendor.setIsActive(true);
        vendorRepository.save(vendor);
    }

    @Transactional
    public void deleteVendor(Long vendorId) {
        if (!vendorRepository.existsById(vendorId)) {
            throw new RuntimeException("Vendor not found with ID: " + vendorId);
        }
        vendorRepository.deleteById(vendorId);
    }

    public Vendors getBuyerVendorId( Long companyId, Long customersVendorId) {
        return vendorRepository.findByCompanyCompanyIdAndVendorCompanyCompanyId(companyId, 
                customersVendorId)
                .orElseThrow(() -> new RuntimeException("Vendor " + customersVendorId + " not found for company ID: " + companyId));
    }

    // get vendor by company id and vendorCompany_id
    public Vendors getBuyersVendorId(Long companyId, Long vendorCompanyId) {
        return vendorRepository.findByCompanyCompanyIdAndVendorCompanyCompanyId(companyId, vendorCompanyId)
                .orElseThrow(() -> new RuntimeException(
                        "Vendor " + vendorCompanyId + " not found for company ID: " + companyId));
    }
}
