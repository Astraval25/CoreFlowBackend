package com.astraval.coreflow.modules.invitation;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.invitation.dto.AcceptInvitationDto;
import com.astraval.coreflow.modules.invitation.dto.InvitationViewDto;
import com.astraval.coreflow.modules.vendor.VendorRepository;
import com.astraval.coreflow.modules.vendor.Vendors;

@Service
public class InvitationService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String TYPE_CUSTOMER = "CUSTOMER";
    private static final String TYPE_VENDOR = "VENDOR";

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Transactional
    public Invitation sendCustomerInvite(Long companyId, Long customerId, String emailOverride) {
        Companies fromCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        Customers customer = customerRepository.findByCustomerIdAndCompanyCompanyId(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        if (customer.getCustomerCompany() != null) {
            throw new RuntimeException("Customer already linked to a company");
        }

        Invitation invitation = new Invitation();
        invitation.setFromCompany(fromCompany);
        invitation.setEmail(emailOverride != null && !emailOverride.isBlank() ? emailOverride : customer.getEmail());
        invitation.setStatus(STATUS_PENDING);
        invitation.setSendAt(LocalDateTime.now());
        invitation.setIsActive(true);
        invitation.setRequestedEntityType(TYPE_CUSTOMER);
        invitation.setRequestedEntityId(customerId);
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());

        return invitationRepository.save(invitation);
    }

    @Transactional
    public Invitation sendVendorInvite(Long companyId, Long vendorId, String emailOverride) {
        Companies fromCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        Vendors vendor = vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

        if (vendor.getVendorCompany() != null) {
            throw new RuntimeException("Vendor already linked to a company");
        }

        Invitation invitation = new Invitation();
        invitation.setFromCompany(fromCompany);
        invitation.setEmail(emailOverride != null && !emailOverride.isBlank() ? emailOverride : vendor.getEmail());
        invitation.setStatus(STATUS_PENDING);
        invitation.setSendAt(LocalDateTime.now());
        invitation.setIsActive(true);
        invitation.setRequestedEntityType(TYPE_VENDOR);
        invitation.setRequestedEntityId(vendorId);
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());

        return invitationRepository.save(invitation);
    }

    public InvitationViewDto getInvitationByCode(UUID code) {
        Invitation invitation = invitationRepository.findByInvitationCode(code)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        return new InvitationViewDto(
                invitation.getInviteId(),
                invitation.getInvitationCode(),
                invitation.getStatus(),
                invitation.getRequestedEntityType(),
                invitation.getRequestedEntityId(),
                invitation.getFromCompany() != null ? invitation.getFromCompany().getCompanyId() : null,
                invitation.getFromCompany() != null ? invitation.getFromCompany().getCompanyName() : null,
                invitation.getToCompany() != null ? invitation.getToCompany().getCompanyId() : null,
                invitation.getEmail(),
                invitation.getSendAt(),
                invitation.getAccespedAt());
    }

    @Transactional
    public void acceptInvitation(Long companyId, UUID code, AcceptInvitationDto request) {
        Invitation invitation = invitationRepository.findByInvitationCode(code)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!STATUS_PENDING.equals(invitation.getStatus())) {
            throw new RuntimeException("Invitation is not pending");
        }

        Companies acceptingCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        if (TYPE_CUSTOMER.equals(invitation.getRequestedEntityType())) {
            if (request.getSelectedVendorId() == null) {
                throw new RuntimeException("Selected vendor is required to accept this invitation");
            }
            Customers customer = customerRepository.findById(invitation.getRequestedEntityId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + invitation.getRequestedEntityId()));

            Vendors vendor = vendorRepository.findByVendorIdAndCompanyCompanyId(request.getSelectedVendorId(), companyId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + request.getSelectedVendorId()));

            if (customer.getCustomerCompany() != null) {
                throw new RuntimeException("Customer already linked to a company");
            }
            if (vendor.getVendorCompany() != null) {
                throw new RuntimeException("Vendor already linked to a company");
            }

            customer.setCustomerCompany(acceptingCompany);
            customer.setAcceptedInvitationId(invitation.getInvitationCode().toString());
            customerRepository.save(customer);

            vendor.setVendorCompany(invitation.getFromCompany());
            vendor.setAcceptedInvitationId(invitation.getInvitationCode().toString());
            vendorRepository.save(vendor);

            invitation.setSelectedVendorId(request.getSelectedVendorId());

        } else if (TYPE_VENDOR.equals(invitation.getRequestedEntityType())) {
            if (request.getSelectedCustomerId() == null) {
                throw new RuntimeException("Selected customer is required to accept this invitation");
            }
            Vendors vendor = vendorRepository.findById(invitation.getRequestedEntityId())
                    .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + invitation.getRequestedEntityId()));

            Customers customer = customerRepository.findByCustomerIdAndCompanyCompanyId(request.getSelectedCustomerId(), companyId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getSelectedCustomerId()));

            if (vendor.getVendorCompany() != null) {
                throw new RuntimeException("Vendor already linked to a company");
            }
            if (customer.getCustomerCompany() != null) {
                throw new RuntimeException("Customer already linked to a company");
            }

            vendor.setVendorCompany(acceptingCompany);
            vendor.setAcceptedInvitationId(invitation.getInvitationCode().toString());
            vendorRepository.save(vendor);

            customer.setCustomerCompany(invitation.getFromCompany());
            customer.setAcceptedInvitationId(invitation.getInvitationCode().toString());
            customerRepository.save(customer);

            invitation.setSelectedCustomerId(request.getSelectedCustomerId());
        } else {
            throw new RuntimeException("Invalid invitation type");
        }

        invitation.setToCompany(acceptingCompany);
        invitation.setStatus(STATUS_ACCEPTED);
        invitation.setAccespedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    @Transactional
    public void rejectInvitation(Long companyId, UUID code) {
        Invitation invitation = invitationRepository.findByInvitationCode(code)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!STATUS_PENDING.equals(invitation.getStatus())) {
            throw new RuntimeException("Invitation is not pending");
        }

        Companies rejectingCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        invitation.setToCompany(rejectingCompany);
        invitation.setStatus(STATUS_REJECTED);
        invitation.setIsActive(false);
        invitation.setUpdatedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }
}
