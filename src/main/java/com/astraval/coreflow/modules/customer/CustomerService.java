package com.astraval.coreflow.modules.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.global.util.SecurityUtil;
import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.address.AddressRepository;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompaniesRepository;
import com.astraval.coreflow.modules.customer.dto.CreateCustomerRequest;
import com.astraval.coreflow.modules.customer.dto.UpdateCustomerRequest;
import com.astraval.coreflow.modules.customer.projection.CustomerProjection;
import com.astraval.coreflow.modules.address.facade.AddressFacade;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private CompaniesRepository companiesRepository;
    
    @Autowired
    private CustomerMapper customerMapper;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    @Autowired
    private AddressFacade addressFacade;
    
    @Transactional
    public CustomerProjection createCustomer(Integer companyId, CreateCustomerRequest request) {
        // Get current user info
        String userIdStr = securityUtil.getCurrentSub();
        Integer userId = Integer.valueOf(userIdStr);
        
        // Get company by provided companyId
        Companies company = companiesRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
        
        // Create customer
        Customers customer = customerMapper.toCustomer(request);
        customer.setCompany(company);
        customer.setIsActive(true);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setCreatedBy(Long.valueOf(userId));
        
        // Create billing address if provided
        if (request.getBillingAddress() != null && request.getBillingAddress().getLine1() != null) {
            Address billingAddress = customerMapper.toAddress(request.getBillingAddress());
            billingAddress.setIsActive(true);
            billingAddress.setCreatedBy(userIdStr);
            billingAddress.setCreatedDt(LocalDateTime.now());
            billingAddress = addressRepository.save(billingAddress);
            customer.setBillingAddrId(billingAddress.getAddressId().toString());
        }
        
        // Create shipping address if provided
        if (request.getShippingAddress() != null && request.getShippingAddress().getLine1() != null) {
            Address shippingAddress = customerMapper.toAddress(request.getShippingAddress());
            shippingAddress.setIsActive(true);
            shippingAddress.setCreatedBy(userIdStr);
            shippingAddress.setCreatedDt(LocalDateTime.now());
            shippingAddress = addressRepository.save(shippingAddress);
            customer.setShippingAddrId(shippingAddress.getAddressId().toString());
        } else if (Boolean.TRUE.equals(request.getSameForShipping()) && customer.getBillingAddrId() != null) {
            // Use billing address for shipping if sameForShipping is true
            customer.setShippingAddrId(customer.getBillingAddrId());
        }
        
        customer = customerRepository.save(customer);
        return mapCustomerWithAddresses(customer);
    }
    
    public List<CustomerProjection> getAllCustomers(Integer companyId) {
        
        return customerRepository.findByCompanyCompanyIdAndIsActiveTrue(companyId)
            .stream()
            .map(this::mapCustomerWithAddresses)
            .toList();
    }
    
    public CustomerProjection getCustomerById(Integer companyId, Long customerId) {
        Customers customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
            
        if (!customer.getCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Customer does not belong to the specified company");
        }
        
        if (!customer.getIsActive()) {
            throw new RuntimeException("Customer is not active");
        }
        
        return mapCustomerWithAddresses(customer);
    }
    
    @Transactional
    public CustomerProjection updateCustomer(Integer companyId, Long customerId, UpdateCustomerRequest request) {
        String userIdStr = securityUtil.getCurrentSub();
        
        Customers customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
            
        if (!customer.getCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Customer does not belong to your company");
        }
        
        // Update customer fields
        customer.setCustomerName(request.getCustomerName());
        customer.setDisplayName(request.getDisplayName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setLang(request.getLang());
        customer.setPan(request.getPan());
        customer.setGst(request.getGst());
        customer.setAdvanceAmount(request.getAdvanceAmount());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setUpdateAt(Long.valueOf(userIdStr));
        
        customer = customerRepository.save(customer);
        return mapCustomerWithAddresses(customer);
    }
    
    @Transactional
    public void deactivateCustomer(Integer companyId, Long customerId) {
        String userIdStr = securityUtil.getCurrentSub();
        
        Customers customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
            
        if (!customer.getCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Customer does not belong to your company");
        }
        
        customer.setIsActive(false);
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setUpdateAt(Long.valueOf(userIdStr));
        
        customerRepository.save(customer);
    }
    
    private CustomerProjection mapCustomerWithAddresses(Customers customer) {
        CustomerProjection projection = customerMapper.toProjection(customer);
        
        // Load billing address if exists
        if (customer.getBillingAddrId() != null) {
            try {
                Integer billingAddrId = Integer.valueOf(customer.getBillingAddrId());
                projection.setBillingAddress(addressFacade.getAddressById(billingAddrId));
            } catch (Exception e) {
                // Handle invalid address ID or address not found
            }
        }
        
        // Load shipping address if exists
        if (customer.getShippingAddrId() != null) {
            try {
                Integer shippingAddrId = Integer.valueOf(customer.getShippingAddrId());
                projection.setShippingAddress(addressFacade.getAddressById(shippingAddrId));
            } catch (Exception e) {
                // Handle invalid address ID or address not found
            }
        }
        
        return projection;
    }
}
