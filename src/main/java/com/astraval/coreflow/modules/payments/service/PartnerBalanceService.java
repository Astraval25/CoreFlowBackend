package com.astraval.coreflow.modules.payments.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.payments.model.Payments;
import com.astraval.coreflow.modules.vendor.VendorRepository;
import com.astraval.coreflow.modules.vendor.Vendors;

@Service
public class PartnerBalanceService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void refreshDueAmounts(Long customerId, Long vendorId) {
        refreshCustomerDueAmount(customerId);
        refreshVendorDueAmount(vendorId);
    }

    @Transactional
    public void refreshDueAmountsForOrder(OrderDetails order) {
        if (order == null) {
            return;
        }

        Long customerId = order.getCustomers() != null ? order.getCustomers().getCustomerId() : null;
        Long vendorId = order.getVendors() != null ? order.getVendors().getVendorId() : null;
        refreshDueAmounts(customerId, vendorId);
    }

    @Transactional
    public void refreshDueAmountsForPayment(Payments payment) {
        if (payment == null) {
            return;
        }

        Long customerId = payment.getCustomers() != null ? payment.getCustomers().getCustomerId() : null;
        Long vendorId = payment.getVendors() != null ? payment.getVendors().getVendorId() : null;
        refreshDueAmounts(customerId, vendorId);
    }

    @Transactional
    public void refreshCustomerDueAmount(Long customerId) {
        if (customerId == null) {
            return;
        }

        Double dueAmount = customerRepository.calculateDueAmountFallback(customerId);
        customerRepository.updateDueAmount(customerId, normalizeAmount(dueAmount));
    }

    @Transactional
    public void refreshVendorDueAmount(Long vendorId) {
        if (vendorId == null) {
            return;
        }

        Double dueAmount = vendorRepository.calculateDueAmountFallback(vendorId);
        vendorRepository.updateDueAmount(vendorId, normalizeAmount(dueAmount));
    }

    @Transactional
    public void refreshAllPartyDueAmounts() {
        for (Customers customer : customerRepository.findAll()) {
            refreshCustomerDueAmount(customer.getCustomerId());
        }
        for (Vendors vendor : vendorRepository.findAll()) {
            refreshVendorDueAmount(vendor.getVendorId());
        }
    }

    @Transactional
    public void refreshPartyDueMaterializedView() {
        try {
            jdbcTemplate.execute("SELECT refresh_mv_party_due_summary()");
        } catch (DataAccessException ex) {
            // materialized view is optional
        }
    }

    private double normalizeAmount(Double amount) {
        if (amount == null) {
            return 0.0;
        }
        return amount;
    }
}
