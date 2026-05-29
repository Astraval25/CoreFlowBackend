package com.astraval.coreflow.main_modules.customer;

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
import com.astraval.coreflow.main_modules.customer.dto.CreateUpdateCustomerDto;
import com.astraval.coreflow.main_modules.customer.dto.CustomerContactLookupRequest;
import com.astraval.coreflow.main_modules.customer.dto.CustomerContactLookupResultDto;
import com.astraval.coreflow.main_modules.customer.dto.CustomerOrderPaymentSummaryDto;
import com.astraval.coreflow.main_modules.customer.dto.CustomerOrderSummaryDto;
import com.astraval.coreflow.main_modules.customer.dto.CustomerPaymentSummaryDto;
import com.astraval.coreflow.main_modules.customer.dto.CustomerSummaryDto;
import com.astraval.coreflow.main_modules.notification.NotificationService;
import com.astraval.coreflow.main_modules.orderdetails.OrderStatus;
import com.astraval.coreflow.main_modules.payments.PaymentStatus;
import com.astraval.coreflow.main_modules.payments.service.PartnerBalanceService;
import com.astraval.coreflow.main_modules.user.User;
import com.astraval.coreflow.main_modules.user.UserRepository;
import com.astraval.coreflow.main_modules.vendor.VendorRepository;
import com.astraval.coreflow.main_modules.vendor.Vendors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

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
    private VendorRepository vendorRepository;

    @Autowired
    private ConnectionRequestService connectionRequestService;

    @Transactional
    public Long createCustomer(Long companyId, CreateUpdateCustomerDto request) {
        try {
            Companies company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

            String phoneKey = CustomerPhoneUtil.toLast10PhoneKey(request.getPhone());
            if (phoneKey != null) {
                Optional<Customers> existingCustomer = customerRepository.findFirstByCompanyIdAndPhoneKey(companyId, phoneKey);
                if (existingCustomer.isPresent()) {
                    Customers duplicate = existingCustomer.get();
                    throw new DuplicateCustomerPhoneException(
                            "A customer with this phone already exists in your company. Open existing customer instead.",
                            duplicate.getCustomerId(),
                            phoneKey);
                }
            }

            Customers customer = new Customers();
            customer.setCompany(company);
            customer.setCustomerName(request.getCustomerName());
            customer.setDisplayName(request.getDisplayName());
            customer.setEmail(request.getEmail());
            customer.setPhone(request.getPhone());
            customer.setLang(request.getLang());
            customer.setPan(request.getPan());
            customer.setGst(request.getGst());
            customer.setSameAsBillingAddress(request.isSameAsBillingAddress());

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
                customer.setConnectionStatus(ConnectionStatus.PENDING);
                // Do NOT set customerCompany or create CompanyLink — wait for acceptance
            }

            // Create addresses if provided
            if (request.getBillingAddress() != null) {
                Address billingAddress = addressMapper.toAddress(request.getBillingAddress());
                Address savedBillingAddress = addressService.createAddress(billingAddress);
                customer.setBillingAddrId(savedBillingAddress);
            }

            if (request.getShippingAddress() != null && !request.isSameAsBillingAddress()) {
                Address shippingAddress = addressMapper.toAddress(request.getShippingAddress());
                Address savedShippingAddress = addressService.createAddress(shippingAddress);
                customer.setShippingAddrId(savedShippingAddress);
            } else if (request.isSameAsBillingAddress() && customer.getBillingAddrId() != null) {
                customer.setShippingAddrId(customer.getBillingAddrId());
            }

            Customers savedCustomer = customerRepository.save(customer);

            // Create connection request (auto-creates vendor on the other side + sends notification)
            if (matchedCompany != null) {
                connectionRequestService.createConnectionRequestFromCustomer(savedCustomer, company, matchedCompany);
            }

            partnerBalanceService.refreshCustomerDueAmount(savedCustomer.getCustomerId());
            return savedCustomer.getCustomerId();

        } catch (DuplicateCustomerPhoneException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create customer: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updateCustomer(Long companyId, Long customerId, CreateUpdateCustomerDto request) {
        try {
            Customers customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

            customer.setCustomerName(request.getCustomerName());
            customer.setDisplayName(request.getDisplayName());
            customer.setEmail(request.getEmail());
            customer.setPhone(request.getPhone());
            customer.setLang(request.getLang());
            customer.setPan(request.getPan());
            customer.setGst(request.getGst());
            customer.setSameAsBillingAddress(request.isSameAsBillingAddress());

            // Update billing address
            if (request.getBillingAddress() != null) {
                if (customer.getBillingAddrId() != null) {
                    addressService.updateAddress(customer.getBillingAddrId().getAddressId(), 
                            addressMapper.toAddress(request.getBillingAddress()));
                } else {
                    Address billingAddress = addressService.createAddress(addressMapper.toAddress(request.getBillingAddress()));
                    customer.setBillingAddrId(billingAddress);
                }
            }

            // Update shipping address
            if (request.isSameAsBillingAddress()) {
                customer.setShippingAddrId(customer.getBillingAddrId());
            } else if (request.getShippingAddress() != null) {
                if (customer.getShippingAddrId() != null && !customer.getShippingAddrId().equals(customer.getBillingAddrId())) {
                    addressService.updateAddress(customer.getShippingAddrId().getAddressId(), 
                            addressMapper.toAddress(request.getShippingAddress()));
                } else {
                    Address shippingAddress = addressService.createAddress(addressMapper.toAddress(request.getShippingAddress()));
                    customer.setShippingAddrId(shippingAddress);
                }
            }

            Customers savedCustomer = customerRepository.save(customer);
            // Only attempt auto-link resolution if no connection request flow is active
            if (savedCustomer.getConnectionStatus() == null) {
                resolveLinkedCompanyForCustomer(savedCustomer)
                        .ifPresent(linkedCompany -> upsertCompanyLinkForCustomer(savedCustomer, linkedCompany));
            }
            partnerBalanceService.refreshCustomerDueAmount(customer.getCustomerId());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update customer: " + e.getMessage(), e);
        }
    }

    public List<CustomerSummaryDto> getCustomersByCompany(Long companyId) {
        return applyUnreadCounts(customerRepository.findByCompanyIdSummary(companyId), companyId);
    }

    public List<CustomerSummaryDto> getActiveCustomersByCompany(Long companyId) {
        return applyUnreadCounts(
                customerRepository.findByCompanyCompanyIdAndIsActiveOrderByDisplayName(companyId, true),
                companyId);
    }
    
    public List<CustomerSummaryDto> getUnlinkedCustomersByCompany(Long companyId) {
        return applyUnreadCounts(customerRepository.findUnlinkedByCompanyIdSummary(companyId), companyId);
    }

    public List<Customers> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<CustomerContactLookupResultDto> contactLookup(Long companyId, CustomerContactLookupRequest request) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        List<String> inputPhones = request != null && request.getPhones() != null ? request.getPhones() : List.of();
        if (inputPhones.isEmpty()) {
            return List.of();
        }

        Map<String, Optional<User>> userCacheByPhoneKey = new HashMap<>();
        Map<String, Optional<Customers>> customerCacheByPhoneKey = new HashMap<>();

        return inputPhones.stream()
                .map(inputPhone -> {
                    String phoneKey = CustomerPhoneUtil.toLast10PhoneKey(inputPhone);
                    if (phoneKey == null) {
                        return new CustomerContactLookupResultDto(
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

                    Optional<Customers> existingCustomerOpt = customerCacheByPhoneKey.computeIfAbsent(
                            phoneKey,
                            key -> customerRepository.findFirstByCompanyIdAndPhoneKey(companyId, key));

                    Long accountCompanyId = null;
                    String accountCompanyName = null;
                    if (userOpt.isPresent() && userOpt.get().getDefaultCompany() != null) {
                        accountCompanyId = userOpt.get().getDefaultCompany().getCompanyId();
                        accountCompanyName = userOpt.get().getDefaultCompany().getCompanyName();
                    }

                    return new CustomerContactLookupResultDto(
                            inputPhone,
                            phoneKey,
                            true,
                            accountCompanyId != null,
                            accountCompanyId,
                            accountCompanyName,
                            existingCustomerOpt.map(Customers::getCustomerId).orElse(null));
                })
                .toList();
    }

    public Customers getCustomerById(Long companyId, Long customerId) {
        Customers customer = customerRepository.findByCustomerIdAndCompanyCompanyId(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
        return customer;
    }

    @Transactional
    public void deactivateCustomer(Long customerId) {
        Customers customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
        customer.setIsActive(false);
        customerRepository.save(customer);
    }

    @Transactional
    public void activateCustomer(Long customerId) {
        Customers customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
        customer.setIsActive(true);
        customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new RuntimeException("Customer not found with ID: " + customerId);
        }
        customerRepository.deleteById(customerId);
    }

    // get customer by company id and customerCompany_id
    public Customers getSellersCustomerId(Long companyId, Long customerCompanyId) {
        Optional<Customers> linkedCustomer = companyLinkRepository
                .findByCustomerCompanyCompanyIdAndVendorCompanyCompanyIdAndIsActiveTrue(customerCompanyId, companyId)
                .map(CompanyLink::getCustomer)
                .filter(customer -> customer != null);
        if (linkedCustomer.isPresent()) {
            return linkedCustomer.get();
        }

        Customers fallback = customerRepository.findByCompanyCompanyIdAndCustomerCompanyCompanyId(companyId, customerCompanyId)
                .orElseThrow(() -> new RuntimeException(
                        "Customer " + customerCompanyId + " not found for company ID: " + companyId));
        if (fallback.getCustomerCompany() != null) {
            upsertCompanyLinkForCustomer(fallback, fallback.getCustomerCompany());
        }
        return fallback;
    }

    // get customer by buyer company id and seller company id
    public Customers getBuyersCustomerId(Long companyId, Long customerCompanyId) {
        return getSellersCustomerId(companyId, customerCompanyId);
    }

    @Transactional
    public Customers linkCustomerByPhone(Long companyId, Long customerId) {
        Customers customer = customerRepository.findByCustomerIdAndCompanyCompanyId(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        if (companyLinkRepository.findByCustomerCustomerIdAndIsActiveTrue(customer.getCustomerId()).isPresent()
                || customer.getCustomerCompany() != null) {
            throw new RuntimeException("Customer is already linked to a company");
        }

        String phoneKey = CustomerPhoneUtil.toLast10PhoneKey(customer.getPhone());
        if (phoneKey == null) {
            throw new RuntimeException("Customer phone is missing or invalid for account linking");
        }

        User user = userRepository.findActiveUserByPhoneKey(phoneKey)
                .orElseThrow(() -> new RuntimeException("No CoreFlow account found for this phone"));

        Companies targetCompany = user.getDefaultCompany();
        if (targetCompany == null) {
            throw new RuntimeException("Matched account has no default company to link");
        }
        if (targetCompany.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Cannot link customer to the same company");
        }

        // Create a PENDING connection request instead of immediate linking
        Companies ownerCompany = customer.getCompany();
        customer.setConnectionStatus(ConnectionStatus.PENDING);
        Customers savedCustomer = customerRepository.save(customer);
        connectionRequestService.createConnectionRequestFromCustomer(savedCustomer, ownerCompany, targetCompany);
        return savedCustomer;
    }

    public Optional<Companies> resolveLinkedCompanyForCustomer(Customers customer) {
        if (customer == null || customer.getCustomerId() == null) {
            return Optional.empty();
        }
        Optional<Companies> linkCompany = companyLinkRepository.findByCustomerCustomerIdAndIsActiveTrue(customer.getCustomerId())
                .map(CompanyLink::getCustomerCompany)
                .filter(company -> company != null);
        if (linkCompany.isPresent()) {
            return linkCompany;
        }
        return Optional.ofNullable(customer.getCustomerCompany());
    }

    public Optional<Vendors> resolveLinkedVendorForCustomer(Customers customer) {
        if (customer == null || customer.getCustomerId() == null || customer.getCompany() == null) {
            return Optional.empty();
        }
        Optional<Vendors> linkedVendor = companyLinkRepository.findByCustomerCustomerIdAndIsActiveTrue(customer.getCustomerId())
                .map(CompanyLink::getVendor)
                .filter(vendor -> vendor != null);
        if (linkedVendor.isPresent()) {
            return linkedVendor;
        }
        Optional<Companies> linkedCompany = resolveLinkedCompanyForCustomer(customer);
        if (linkedCompany.isEmpty()) {
            return Optional.empty();
        }
        return vendorRepository.findByCompanyCompanyIdAndVendorCompanyCompanyId(
                linkedCompany.get().getCompanyId(),
                customer.getCompany().getCompanyId());
    }

    private void upsertCompanyLinkForCustomer(Customers customer, Companies linkedCompany) {
        if (customer == null || customer.getCustomerId() == null || customer.getCompany() == null || linkedCompany == null) {
            return;
        }
        Long ownerCompanyId = customer.getCompany().getCompanyId();
        Long linkedCompanyId = linkedCompany.getCompanyId();
        if (ownerCompanyId == null || linkedCompanyId == null || ownerCompanyId.equals(linkedCompanyId)) {
            return;
        }

        CompanyLink link = companyLinkRepository.findByCustomerCustomerId(customer.getCustomerId())
                .or(() -> companyLinkRepository.findByCustomerCompanyCompanyIdAndVendorCompanyCompanyId(
                        linkedCompanyId,
                        ownerCompanyId))
                .orElseGet(CompanyLink::new);

        link.setCustomer(customer);
        link.setCustomerCompany(linkedCompany);
        link.setVendorCompany(customer.getCompany());
        if (link.getVendor() == null) {
            vendorRepository.findByCompanyCompanyIdAndVendorCompanyCompanyId(linkedCompanyId, ownerCompanyId)
                    .ifPresent(link::setVendor);
        }
        link.setIsActive(true);
        companyLinkRepository.save(link);
    }

    public CustomerOrderPaymentSummaryDto getOrdersAndPaymentsByCustomer(
            Long companyId, Long customerId, PaginationRequest paginationRequest) {

        // Validate customer belongs to this company
        customerRepository.findByCustomerIdAndCompanyCompanyId(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        PageRequest pageRequest = PageRequest.of(paginationRequest.getPage(), paginationRequest.getSize());
        String search = paginationRequest.getSearch();

        Page<Object[]> orderResults = customerRepository.findOrdersByCustomerId(customerId, search, pageRequest);
        Page<Object[]> paymentResults = customerRepository.findPaymentsByCustomerId(customerId, search, pageRequest);

        List<CustomerOrderSummaryDto> orders = orderResults.map(row -> new CustomerOrderSummaryDto(
                row[0] != null ? ((Number) row[0]).longValue() : null,
                (String) row[1],
                row[2] != null ? ((Number) row[2]).doubleValue() : null,
                (String) row[3],
                row[4] != null ? ((Number) row[4]).doubleValue() : null,
                row[5] != null ? (LocalDateTime) row[5] : null,
                isViewedOrderStatus((String) row[6])
        )).getContent();

        List<CustomerPaymentSummaryDto> payments = paymentResults.map(row -> new CustomerPaymentSummaryDto(
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

        CustomerOrderPaymentSummaryDto result = new CustomerOrderPaymentSummaryDto(orders, payments);
        result.setPaginationInfo(paginationInfo);
        return result;
    }

    private List<CustomerSummaryDto> applyUnreadCounts(List<CustomerSummaryDto> customers, Long companyId) {
        var unreadCountByCustomer = notificationService.getCompanyUnreadCountBySubjectType(companyId, "CUSTOMER");
        customers.forEach(customer -> customer.setUnreadCount(
                unreadCountByCustomer.getOrDefault(customer.getCustomerId(), 0L)));
        return customers;
    }

    /**
     * Double-tick ("viewed") rule for customer orders.
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
     * Double-tick ("viewed") rule for customer payments.
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
