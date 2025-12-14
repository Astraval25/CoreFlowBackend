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
    
    @Transactional
    public CustomerProjection createCustomer(CreateCustomerRequest request) {
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
        return customerMapper.toProjection(customer);
    }
    
    public List<CustomerProjection> getAllCustomers() {
        Integer companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new RuntimeException("Company ID not found in token");
        }
        
        return customerRepository.findByCompanyCompanyIdAndIsActiveTrue(companyId)
            .stream()
            .map(customerMapper::toProjection)
            .toList();
    }
    
    @Transactional
    public CustomerProjection updateCustomer(Long customerId, UpdateCustomerRequest request) {
        String userIdStr = securityUtil.getCurrentSub();
        Integer companyId = securityUtil.getCurrentCompanyId();
        
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
        return customerMapper.toProjection(customer);
    }
    
    @Transactional
    public void deactivateCustomer(Long customerId) {
        String userIdStr = securityUtil.getCurrentSub();
        Integer companyId = securityUtil.getCurrentCompanyId();
        
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
}
