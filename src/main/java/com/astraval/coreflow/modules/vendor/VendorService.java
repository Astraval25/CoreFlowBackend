package com.astraval.coreflow.modules.vendor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.common.util.PaginationInfo;
import com.astraval.coreflow.common.util.PaginationRequest;
import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.address.AddressMapper;
import com.astraval.coreflow.modules.address.AddressService;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.payments.service.PartnerBalanceService;
import com.astraval.coreflow.modules.vendor.dto.CreateUpdateVendorDto;
import com.astraval.coreflow.modules.vendor.dto.VendorOrderPaymentSummaryDto;
import com.astraval.coreflow.modules.vendor.dto.VendorOrderSummaryDto;
import com.astraval.coreflow.modules.vendor.dto.VendorPaymentSummaryDto;
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

    @Autowired
    private PartnerBalanceService partnerBalanceService;


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

            Vendors savedVendor = vendorRepository.save(vendor);
            partnerBalanceService.refreshVendorDueAmount(savedVendor.getVendorId());
            return savedVendor.getVendorId();

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
            partnerBalanceService.refreshVendorDueAmount(vendor.getVendorId());
            
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
                        "Vendor link not found. Expected vendors row with comp_id=" + companyId +
                                " and vendor_comp_id=" + vendorCompanyId));
    }

    public VendorOrderPaymentSummaryDto getOrdersAndPaymentsByVendor(
            Long companyId, Long vendorId, PaginationRequest paginationRequest) {

        vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

        PageRequest pageRequest = PageRequest.of(paginationRequest.getPage(), paginationRequest.getSize());
        String search = paginationRequest.getSearch();

        Page<Object[]> orderResults = vendorRepository.findOrdersByVendorId(vendorId, search, pageRequest);
        Page<Object[]> paymentResults = vendorRepository.findPaymentsByVendorId(vendorId, search, pageRequest);

        List<VendorOrderSummaryDto> orders = orderResults.map(row -> new VendorOrderSummaryDto(
                row[0] != null ? ((Number) row[0]).longValue() : null,
                (String) row[1],
                row[2] != null ? ((Number) row[2]).doubleValue() : null,
                (String) row[3],
                row[4] != null ? ((Number) row[4]).doubleValue() : null,
                row[5] != null ? (LocalDateTime) row[5] : null
        )).getContent();

        List<VendorPaymentSummaryDto> payments = paymentResults.map(row -> new VendorPaymentSummaryDto(
                row[0] != null ? ((Number) row[0]).longValue() : null,
                (String) row[1],
                row[2] != null ? (LocalDateTime) row[2] : null,
                row[3] != null ? ((Number) row[3]).doubleValue() : null
        )).getContent();

        PaginationInfo paginationInfo = new PaginationInfo(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                orderResults.getTotalElements() + paymentResults.getTotalElements(),
                Math.max(orderResults.getTotalPages(), paymentResults.getTotalPages()),
                orderResults.hasNext() || paymentResults.hasNext(),
                orderResults.hasPrevious() || paymentResults.hasPrevious(),
                paginationRequest.getSearch(),
                paginationRequest.getSortBy(),
                paginationRequest.getSortDirection()
        );

        VendorOrderPaymentSummaryDto result = new VendorOrderPaymentSummaryDto(orders, payments);
        result.setPaginationInfo(paginationInfo);
        return result;
    }
}
