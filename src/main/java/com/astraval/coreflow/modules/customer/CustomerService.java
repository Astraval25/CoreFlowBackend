package com.astraval.coreflow.modules.customer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.address.AddressMapper;
import com.astraval.coreflow.modules.address.AddressService;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.customer.dto.CreateUpdateCustomerDto;
import com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto;

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

    @Transactional
    public Long createCustomer(Long companyId, CreateUpdateCustomerDto request) {
        try {
            Companies company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

            Customers customer = new Customers();
            customer.setCompany(company);
            customer.setCustomerName(request.getCustomerName());
            customer.setDisplayName(request.getDisplayName());
            customer.setEmail(request.getEmail());
            customer.setPhone(request.getPhone());
            customer.setLang(request.getLang());
            customer.setPan(request.getPan());
            customer.setGst(request.getGst());
            customer.setDueAmount(request.getDueAmount());
            customer.setSameAsBillingAddress(request.isSameAsBillingAddress());

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

            return customerRepository.save(customer).getCustomerId();

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
            customer.setDueAmount(request.getDueAmount());
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

            customerRepository.save(customer);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update customer: " + e.getMessage(), e);
        }
    }

    public List<CustomerSummaryDto> getCustomersByCompany(Long companyId) {
        return customerRepository.findByCompanyIdSummary(companyId);
    }

    public List<CustomerSummaryDto> getActiveCustomersByCompany(Long companyId) {
        return customerRepository.findByCompanyCompanyIdAndIsActiveOrderByDisplayName(companyId, true);
    }
    
    public List<CustomerSummaryDto> getUnlinkedCustomersByCompany(Long companyId) {
        return customerRepository.findUnlinkedByCompanyIdSummary(companyId);
    }

    public List<Customers> getAllCustomers() {
        return customerRepository.findAll();
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
        return customerRepository.findByCompanyCompanyIdAndCustomerCompanyCompanyId(companyId, customerCompanyId)
                .orElseThrow(() -> new RuntimeException(
                        "Customer " + customerCompanyId + " not found for company ID: " + companyId));
    }

}
