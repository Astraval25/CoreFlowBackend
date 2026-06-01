package com.astraval.coreflow.main_modules.vendor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.common.util.PaginationInfo;
import com.astraval.coreflow.common.util.PaginationRequest;
import com.astraval.coreflow.main_modules.address.Address;
import com.astraval.coreflow.main_modules.address.AddressMapper;
import com.astraval.coreflow.main_modules.address.AddressService;
import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.companylink.CompanyLink;
import com.astraval.coreflow.main_modules.companylink.CompanyLinkRepository;
import com.astraval.coreflow.main_modules.companylink.ConnectionRequestService;
import com.astraval.coreflow.main_modules.companylink.ConnectionStatus;
import com.astraval.coreflow.main_modules.customer.CustomerPhoneUtil;
import com.astraval.coreflow.main_modules.customer.CustomerRepository;
import com.astraval.coreflow.main_modules.customer.Customers;
import com.astraval.coreflow.main_modules.payments.service.PartnerBalanceService;
import com.astraval.coreflow.main_modules.notification.NotificationService;
import com.astraval.coreflow.main_modules.orderdetails.OrderStatus;
import com.astraval.coreflow.main_modules.payments.PaymentStatus;
import com.astraval.coreflow.main_modules.user.User;
import com.astraval.coreflow.main_modules.user.UserRepository;
import com.astraval.coreflow.main_modules.vendor.dto.CreateUpdateVendorDto;
import com.astraval.coreflow.main_modules.vendor.dto.VendorContactLookupRequest;
import com.astraval.coreflow.main_modules.vendor.dto.VendorContactLookupResultDto;
import com.astraval.coreflow.main_modules.vendor.dto.VendorOrderPaymentSummaryDto;
import com.astraval.coreflow.main_modules.vendor.dto.VendorOrderSummaryDto;
import com.astraval.coreflow.main_modules.vendor.dto.VendorPaymentSummaryDto;
import com.astraval.coreflow.main_modules.vendor.dto.VendorSummaryDto;

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

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyLinkRepository companyLinkRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ConnectionRequestService connectionRequestService;

    @Transactional
    public Long createVendor(Long companyId, CreateUpdateVendorDto request) {
        try {
            Companies company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

            String phoneKey = CustomerPhoneUtil.toLast10PhoneKey(request.getPhone());
            if (phoneKey != null) {
                Optional<Vendors> existingVendor = vendorRepository.findFirstByCompanyIdAndPhoneKey(companyId, phoneKey);
                if (existingVendor.isPresent()) {
                    Vendors duplicate = existingVendor.get();
                    throw new DuplicateVendorPhoneException(
                            "A vendor with this phone already exists in your company. Open existing vendor instead.",
                            duplicate.getVendorId(),
                            phoneKey);
                }
            }

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

            // Check if phone matches an existing user's company — create PENDING connection request
            Companies matchedCompany = null;
            if (phoneKey != null) {
                matchedCompany = userRepository.findActiveUserByPhoneKey(phoneKey)
                        .map(User::getDefaultCompany)
                        .filter(linkedCompany -> linkedCompany != null
                                && !linkedCompany.getCompanyId().equals(companyId))
                        .orElse(null);
            }

            if (matchedCompany != null) {
                vendor.setConnectionStatus(ConnectionStatus.PENDING);
                vendor.setVendorCompany(matchedCompany);
                // Do NOT create CompanyLink — wait for mutual acceptance
            }

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

            // Create connection request (auto-creates customer on the other side + sends notification)
            if (matchedCompany != null) {
                connectionRequestService.createConnectionRequestFromVendor(savedVendor, company, matchedCompany);
            }

            partnerBalanceService.refreshVendorDueAmount(savedVendor.getVendorId());
            return savedVendor.getVendorId();

        } catch (DuplicateVendorPhoneException e) {
            throw e;
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

            // Only attempt auto-link resolution if no connection request flow is active
            if (vendor.getConnectionStatus() == null && resolveLinkedCompanyForVendor(vendor).isEmpty()) {
                String phoneKey = CustomerPhoneUtil.toLast10PhoneKey(request.getPhone());
                if (phoneKey != null) {
                    userRepository.findActiveUserByPhoneKey(phoneKey)
                            .map(User::getDefaultCompany)
                            .filter(linkedCompany -> linkedCompany != null
                                    && !linkedCompany.getCompanyId().equals(companyId))
                            .ifPresent(linkedCompany -> {
                                vendor.setVendorCompany(linkedCompany);
                                upsertCompanyLinkForVendor(vendor, linkedCompany);
                            });
                }
            }

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

            Vendors savedVendor = vendorRepository.save(vendor);
            if (savedVendor.getConnectionStatus() == null) {
                resolveLinkedCompanyForVendor(savedVendor)
                        .ifPresent(linkedCompany -> upsertCompanyLinkForVendor(savedVendor, linkedCompany));
            }
            partnerBalanceService.refreshVendorDueAmount(vendor.getVendorId());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update vendor: " + e.getMessage(), e);
        }
    }

    public List<VendorSummaryDto> getVendorsByCompany(Long companyId) {
        return applyUnreadCounts(vendorRepository.findByCompanyIdSummary(companyId), companyId);
    }

    public List<VendorSummaryDto> getActiveVendorsByCompany(Long companyId) {
        return applyUnreadCounts(
                vendorRepository.findByCompanyCompanyIdAndIsActiveOrderByDisplayName(companyId, true),
                companyId);
    }
    
    public List<VendorSummaryDto> getUnlinkedVendorsByCompany(Long companyId) {
        return applyUnreadCounts(vendorRepository.findUnlinkedByCompanyIdSummary(companyId), companyId);
    }

    public List<Vendors> getAllVendors() {
        return vendorRepository.findAll();
    }

    public List<VendorContactLookupResultDto> contactLookup(Long companyId, VendorContactLookupRequest request) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        List<String> inputPhones = request != null && request.getPhones() != null ? request.getPhones() : List.of();
        if (inputPhones.isEmpty()) {
            return List.of();
        }

        Map<String, Optional<User>> userCacheByPhoneKey = new HashMap<>();
        Map<String, Optional<Vendors>> vendorCacheByPhoneKey = new HashMap<>();

        return inputPhones.stream()
                .map(inputPhone -> {
                    String phoneKey = CustomerPhoneUtil.toLast10PhoneKey(inputPhone);
                    if (phoneKey == null) {
                        return new VendorContactLookupResultDto(
                                inputPhone,
                                null,
                                false,
                                false,
                                null,
                                null,
                                null);
                    }

                    Optional<User> userOpt = userCacheByPhoneKey.computeIfAbsent(
                            phoneKey,
                            key -> userRepository.findActiveUserByPhoneKey(key));

                    Optional<Vendors> existingVendorOpt = vendorCacheByPhoneKey.computeIfAbsent(
                            phoneKey,
                            key -> vendorRepository.findFirstByCompanyIdAndPhoneKey(companyId, key));

                    Long accountCompanyId = null;
                    String accountCompanyName = null;
                    if (userOpt.isPresent() && userOpt.get().getDefaultCompany() != null) {
                        accountCompanyId = userOpt.get().getDefaultCompany().getCompanyId();
                        accountCompanyName = userOpt.get().getDefaultCompany().getCompanyName();
                    }

                    return new VendorContactLookupResultDto(
                            inputPhone,
                            phoneKey,
                            true,
                            accountCompanyId != null,
                            accountCompanyId,
                            accountCompanyName,
                            existingVendorOpt.map(Vendors::getVendorId).orElse(null));
                })
                .toList();
    }

    public Vendors getVendorById(Long companyId, Long vendorId) {
        Vendors vendor = vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));
        return vendor;
    }

    @Transactional
    public Vendors linkVendorByPhone(Long companyId, Long vendorId) {
        Vendors vendor = vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

        if (companyLinkRepository.findByVendorVendorIdAndIsActiveTrue(vendor.getVendorId()).isPresent()
                || vendor.getVendorCompany() != null) {
            throw new RuntimeException("Vendor is already linked to a company");
        }

        String phoneKey = CustomerPhoneUtil.toLast10PhoneKey(vendor.getPhone());
        if (phoneKey == null) {
            throw new RuntimeException("Vendor phone is missing or invalid for account linking");
        }

        User user = userRepository.findActiveUserByPhoneKey(phoneKey)
                .orElseThrow(() -> new RuntimeException("No CoreFlow account found for this phone"));

        Companies targetCompany = user.getDefaultCompany();
        if (targetCompany == null) {
            throw new RuntimeException("Matched account has no default company to link");
        }
        if (targetCompany.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Cannot link vendor to the same company");
        }

        // Create a PENDING connection request instead of immediate linking
        Companies ownerCompany = vendor.getCompany();
        vendor.setConnectionStatus(ConnectionStatus.PENDING);
        vendor.setVendorCompany(targetCompany);
        Vendors savedVendor = vendorRepository.save(vendor);
        connectionRequestService.createConnectionRequestFromVendor(savedVendor, ownerCompany, targetCompany);
        return savedVendor;
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
        Optional<Vendors> linkedVendor = companyLinkRepository
                .findByCustomerCompanyCompanyIdAndVendorCompanyCompanyIdAndIsActiveTrue(companyId, customersVendorId)
                .map(CompanyLink::getVendor)
                .filter(vendor -> vendor != null);
        if (linkedVendor.isPresent()) {
            return linkedVendor.get();
        }

        Vendors fallback = vendorRepository.findByCompanyCompanyIdAndVendorCompanyCompanyId(companyId, customersVendorId)
                .orElseThrow(() -> new RuntimeException(
                        "Linked company setup incomplete: company " + companyId
                                + " has not created a vendor linked to your company " + customersVendorId
                                + ". Ask them to create vendor first, then retry order creation."));
        if (fallback.getVendorCompany() != null) {
            upsertCompanyLinkForVendor(fallback, fallback.getVendorCompany());
        }
        return fallback;
    }

    // get vendor by company id and vendorCompany_id
    public Vendors getBuyersVendorId(Long companyId, Long vendorCompanyId) {
        return getBuyerVendorId(companyId, vendorCompanyId);
    }

    public Optional<Companies> resolveLinkedCompanyForVendor(Vendors vendor) {
        if (vendor == null || vendor.getVendorId() == null) {
            return Optional.empty();
        }
        Optional<Companies> linkCompany = companyLinkRepository.findByVendorVendorIdAndIsActiveTrue(vendor.getVendorId())
                .map(CompanyLink::getVendorCompany)
                .filter(company -> company != null);
        if (linkCompany.isPresent()) {
            return linkCompany;
        }
        return Optional.ofNullable(vendor.getVendorCompany());
    }

    public Optional<Customers> resolveLinkedCustomerForVendor(Vendors vendor) {
        if (vendor == null || vendor.getVendorId() == null || vendor.getCompany() == null) {
            return Optional.empty();
        }
        Optional<Customers> linkedCustomer = companyLinkRepository.findByVendorVendorIdAndIsActiveTrue(vendor.getVendorId())
                .map(CompanyLink::getCustomer)
                .filter(customer -> customer != null);
        if (linkedCustomer.isPresent()) {
            return linkedCustomer;
        }
        Optional<Companies> linkedCompany = resolveLinkedCompanyForVendor(vendor);
        if (linkedCompany.isEmpty()) {
            return Optional.empty();
        }
        return customerRepository.findByCompanyCompanyIdAndCustomerCompanyCompanyId(
                linkedCompany.get().getCompanyId(),
                vendor.getCompany().getCompanyId());
    }

    private void upsertCompanyLinkForVendor(Vendors vendor, Companies linkedCompany) {
        if (vendor == null || vendor.getVendorId() == null || vendor.getCompany() == null || linkedCompany == null) {
            return;
        }
        Long ownerCompanyId = vendor.getCompany().getCompanyId();
        Long linkedCompanyId = linkedCompany.getCompanyId();
        if (ownerCompanyId == null || linkedCompanyId == null || ownerCompanyId.equals(linkedCompanyId)) {
            return;
        }

        CompanyLink link = companyLinkRepository.findByVendorVendorId(vendor.getVendorId())
                .or(() -> companyLinkRepository.findByCustomerCompanyCompanyIdAndVendorCompanyCompanyId(
                        ownerCompanyId,
                        linkedCompanyId))
                .orElseGet(CompanyLink::new);

        link.setVendor(vendor);
        link.setVendorCompany(linkedCompany);
        link.setCustomerCompany(vendor.getCompany());
        if (link.getCustomer() == null) {
            customerRepository.findByCompanyCompanyIdAndCustomerCompanyCompanyId(linkedCompanyId, ownerCompanyId)
                    .ifPresent(link::setCustomer);
        }
        link.setIsActive(true);
        companyLinkRepository.save(link);
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
                row[5] != null ? (LocalDateTime) row[5] : null,
                isViewedOrderStatus((String) row[6])
        )).getContent();

        List<VendorPaymentSummaryDto> payments = paymentResults.map(row -> new VendorPaymentSummaryDto(
                row[0] != null ? ((Number) row[0]).longValue() : null,
                (String) row[1],
                row[2] != null ? (LocalDateTime) row[2] : null,
                row[3] != null ? ((Number) row[3]).doubleValue() : null,
                isViewedPaymentStatus((String) row[4])
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

    private List<VendorSummaryDto> applyUnreadCounts(List<VendorSummaryDto> vendors, Long companyId) {
        var unreadCountByVendor = notificationService.getCompanyUnreadCountBySubjectType(companyId, "VENDOR");
        vendors.forEach(vendor -> vendor.setUnreadCount(
                unreadCountByVendor.getOrDefault(vendor.getVendorId(), 0L)));
        return vendors;
    }

    /**
     * Double-tick ("viewed") rule for vendor orders.
     *
     * We intentionally infer this from lifecycle status rather than storing a
     * separate viewed flag. Any status at or after viewed in the flow is
     * considered viewed in client UI.
     */
    private boolean isViewedOrderStatus(String status) {
        if (status == null || status.isBlank()) return false;
        return OrderStatus.getOrderViewed().equalsIgnoreCase(status)
                || OrderStatus.getOrderInvoiced().equalsIgnoreCase(status)
                || OrderStatus.getOrderPayed().equalsIgnoreCase(status);
    }

    /**
     * Double-tick ("viewed") rule for vendor payments.
     *
     * Payments are treated as viewed when they enter viewed or any terminal /
     * decision state.
     */
    private boolean isViewedPaymentStatus(String status) {
        if (status == null || status.isBlank()) return false;
        return PaymentStatus.getPaymentViewed().equalsIgnoreCase(status)
                || PaymentStatus.getPaymentAccepted().equalsIgnoreCase(status)
                || PaymentStatus.getPaymentDeclined().equalsIgnoreCase(status)
                || PaymentStatus.getPartiallyPaid().equalsIgnoreCase(status)
                || PaymentStatus.getPaymentRefund().equalsIgnoreCase(status)
                || PaymentStatus.getPaymentFailed().equalsIgnoreCase(status);
    }
}
