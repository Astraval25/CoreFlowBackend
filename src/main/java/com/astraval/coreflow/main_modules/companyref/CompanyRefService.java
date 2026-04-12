package com.astraval.coreflow.main_modules.companyref;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.orderdetails.OrderDetails;
import com.astraval.coreflow.main_modules.payments.model.Payments;

@Service
public class CompanyRefService {

    @Autowired
    private CompanyOrderRefRepository orderRefRepo;

    @Autowired
    private CompanyPaymentRefRepository paymentRefRepo;

    @Autowired
    private CompanyRepository companyRepository;

    public CompanyOrderRef createOrderRef(Long companyId, OrderDetails order, String localNumber) {
        Companies company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        CompanyOrderRef ref = new CompanyOrderRef();
        ref.setCompany(company);
        ref.setOrderDetails(order);
        ref.setLocalOrderNumber(localNumber);
        return orderRefRepo.save(ref);
    }

    public CompanyPaymentRef createPaymentRef(Long companyId, Payments payment, String localNumber) {
        Companies company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        CompanyPaymentRef ref = new CompanyPaymentRef();
        ref.setCompany(company);
        ref.setPayment(payment);
        ref.setLocalPaymentNumber(localNumber);
        return paymentRefRepo.save(ref);
    }

    public Optional<CompanyOrderRef> getOrderRef(Long companyId, Long orderId) {
        return orderRefRepo.findByCompanyCompanyIdAndOrderDetailsOrderId(companyId, orderId);
    }

    public Optional<CompanyPaymentRef> getPaymentRef(Long companyId, Long paymentId) {
        return paymentRefRepo.findByCompanyCompanyIdAndPaymentPaymentId(companyId, paymentId);
    }

    public Map<Long, CompanyOrderRef> getOrderRefsBatch(Long companyId, List<Long> orderIds) {
        return orderRefRepo.findByCompanyCompanyIdAndOrderDetailsOrderIdIn(companyId, orderIds)
            .stream()
            .collect(Collectors.toMap(
                ref -> ref.getOrderDetails().getOrderId(),
                ref -> ref,
                (a, b) -> a
            ));
    }

    public Map<Long, CompanyPaymentRef> getPaymentRefsBatch(Long companyId, List<Long> paymentIds) {
        return paymentRefRepo.findByCompanyCompanyIdAndPaymentPaymentIdIn(companyId, paymentIds)
            .stream()
            .collect(Collectors.toMap(
                ref -> ref.getPayment().getPaymentId(),
                ref -> ref,
                (a, b) -> a
            ));
    }

    public void updateOrderRef(Long companyId, Long orderId,
                                String internalRemarks, String internalStatus,
                                String internalTags, String customReference) {
        CompanyOrderRef ref = orderRefRepo
            .findByCompanyCompanyIdAndOrderDetailsOrderId(companyId, orderId)
            .orElseThrow(() -> new RuntimeException("Order ref not found for company " + companyId + " order " + orderId));

        if (internalRemarks != null) ref.setInternalRemarks(internalRemarks);
        if (internalStatus != null) ref.setInternalStatus(internalStatus);
        if (internalTags != null) ref.setInternalTags(internalTags);
        if (customReference != null) ref.setCustomReference(customReference);
        orderRefRepo.save(ref);
    }

    public void updatePaymentRef(Long companyId, Long paymentId,
                                  String internalRemarks, String internalStatus,
                                  String customReference) {
        CompanyPaymentRef ref = paymentRefRepo
            .findByCompanyCompanyIdAndPaymentPaymentId(companyId, paymentId)
            .orElseThrow(() -> new RuntimeException("Payment ref not found for company " + companyId + " payment " + paymentId));

        if (internalRemarks != null) ref.setInternalRemarks(internalRemarks);
        if (internalStatus != null) ref.setInternalStatus(internalStatus);
        if (customReference != null) ref.setCustomReference(customReference);
        paymentRefRepo.save(ref);
    }
}
