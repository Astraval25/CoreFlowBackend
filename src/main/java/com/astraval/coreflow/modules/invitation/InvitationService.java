package com.astraval.coreflow.modules.invitation;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.customer.CustomerVendorLink;
import com.astraval.coreflow.modules.customer.CustomerVendorLinkRepository;
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
    private static final String INVITATION_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITATION_CODE_LENGTH = 6;
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CustomerVendorLinkRepository customerVendorLinkRepository;

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
        invitation.setInvitationCode(generateUniqueInvitationCode());
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
        invitation.setInvitationCode(generateUniqueInvitationCode());
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());

        return invitationRepository.save(invitation);
    }

    public InvitationViewDto getInvitationByCode(String code) {
        String normalizedCode = normalizeInvitationCode(code);
        Invitation invitation = invitationRepository.findByInvitationCode(normalizedCode)
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

    public Invitation getLatestCustomerInvitationByCreator(Long companyId, Long customerId) {
        customerRepository.findByCustomerIdAndCompanyCompanyId(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        return invitationRepository
                .findTopByFromCompanyCompanyIdAndRequestedEntityTypeAndRequestedEntityIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(
                        companyId, TYPE_CUSTOMER, customerId, STATUS_PENDING)
                .orElseThrow(() -> new RuntimeException("No active pending invitation found for customer ID: " + customerId));
    }

    public Invitation getLatestVendorInvitationByCreator(Long companyId, Long vendorId) {
        vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

        return invitationRepository
                .findTopByFromCompanyCompanyIdAndRequestedEntityTypeAndRequestedEntityIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(
                        companyId, TYPE_VENDOR, vendorId, STATUS_PENDING)
                .orElseThrow(() -> new RuntimeException("No active pending invitation found for vendor ID: " + vendorId));
    }

    @Transactional
    public void acceptInvitation(Long companyId, String code, AcceptInvitationDto request) {
        String normalizedCode = normalizeInvitationCode(code);
        Invitation invitation = invitationRepository.findByInvitationCode(normalizedCode)
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
            if (customer.getCompany().getCompanyId().equals(vendor.getCompany().getCompanyId())) {
                throw new RuntimeException("Customer and vendor cannot belong to the same company");
            }

            customer.setCustomerCompany(acceptingCompany);
            customer.setAcceptedInvitationId(invitation.getInvitationCode());
            customerRepository.save(customer);

            vendor.setVendorCompany(invitation.getFromCompany());
            vendor.setAcceptedInvitationId(invitation.getInvitationCode());
            vendorRepository.save(vendor);

            upsertCustomerVendorLink(customer, vendor);

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
            if (vendor.getCompany().getCompanyId().equals(customer.getCompany().getCompanyId())) {
                throw new RuntimeException("Customer and vendor cannot belong to the same company");
            }

            vendor.setVendorCompany(acceptingCompany);
            vendor.setAcceptedInvitationId(invitation.getInvitationCode());
            vendorRepository.save(vendor);

            customer.setCustomerCompany(invitation.getFromCompany());
            customer.setAcceptedInvitationId(invitation.getInvitationCode());
            customerRepository.save(customer);

            upsertCustomerVendorLink(customer, vendor);

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
    public void rejectInvitation(Long companyId, String code) {
        String normalizedCode = normalizeInvitationCode(code);
        Invitation invitation = invitationRepository.findByInvitationCode(normalizedCode)
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

    private String generateUniqueInvitationCode() {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            String code = randomInvitationCode();
            if (!invitationRepository.existsByInvitationCode(code)) {
                return code;
            }
        }
        throw new RuntimeException("Failed to generate unique invitation code. Please try again.");
    }

    private String randomInvitationCode() {
        StringBuilder code = new StringBuilder(INVITATION_CODE_LENGTH);
        for (int i = 0; i < INVITATION_CODE_LENGTH; i++) {
            int idx = RANDOM.nextInt(INVITATION_CODE_ALPHABET.length());
            code.append(INVITATION_CODE_ALPHABET.charAt(idx));
        }
        return code.toString();
    }

    private String normalizeInvitationCode(String code) {
        if (code == null || code.isBlank()) {
            throw new RuntimeException("Invitation code is required");
        }

        String normalized = code.trim().toUpperCase();
        if (normalized.length() != INVITATION_CODE_LENGTH) {
            throw new RuntimeException("Invitation code must be 6 characters");
        }

        for (int i = 0; i < normalized.length(); i++) {
            if (INVITATION_CODE_ALPHABET.indexOf(normalized.charAt(i)) < 0) {
                throw new RuntimeException("Invitation code format is invalid");
            }
        }

        return normalized;
    }

    private void upsertCustomerVendorLink(Customers customer, Vendors vendor) {
        CustomerVendorLink link = customerVendorLinkRepository.findByCustomerCustomerId(customer.getCustomerId())
                .orElseGet(CustomerVendorLink::new);

        link.setCustomer(customer);
        link.setVendor(vendor);
        link.setCustomerCompanyId(customer.getCompany().getCompanyId());
        link.setVendorCompanyId(vendor.getCompany().getCompanyId());
        link.setIsActive(true);
        customerVendorLinkRepository.save(link);
    }
}
