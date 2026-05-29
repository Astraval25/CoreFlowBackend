package com.astraval.coreflow.main_modules.companylink;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.customer.CustomerRepository;
import com.astraval.coreflow.main_modules.customer.Customers;
import com.astraval.coreflow.main_modules.invitation.Invitation;
import com.astraval.coreflow.main_modules.invitation.InvitationRepository;
import com.astraval.coreflow.main_modules.notification.NotificationService;
import com.astraval.coreflow.main_modules.payments.service.PartnerBalanceService;
import com.astraval.coreflow.main_modules.vendor.VendorRepository;
import com.astraval.coreflow.main_modules.vendor.Vendors;

/**
 * Handles the consent-based connection request flow between companies.
 *
 * When Company A creates a Customer/Vendor whose phone matches Company B's user,
 * instead of auto-linking, a PENDING connection request is created.
 * Company B must accept/reject the request. Only after acceptance
 * is the CompanyLink created and orders/payments allowed.
 */
@Service
public class ConnectionRequestService {

    private static final String REQUEST_TYPE_AUTO = "AUTO";
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
    private CompanyLinkRepository companyLinkRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PartnerBalanceService partnerBalanceService;

    /**
     * Called when Company A creates a Customer whose phone matches Company B's user.
     * Creates a PENDING connection request and auto-creates a Vendor on Company B's side.
     *
     * @param customer     the saved Customer record (on Company A's side)
     * @param ownerCompany Company A (the company that created the customer)
     * @param targetCompany Company B (the matched company)
     * @return the auto-created Vendor on Company B's side
     */
    @Transactional
    public Vendors createConnectionRequestFromCustomer(Customers customer, Companies ownerCompany, Companies targetCompany) {
        // Auto-create a Vendor on Company B's side with requester's phone as the name
        String requesterPhone = ownerCompany.getContactPhone() != null
                ? ownerCompany.getContactPhone()
                : customer.getPhone();
        String vendorName = requesterPhone != null ? requesterPhone : ownerCompany.getCompanyName();

        Vendors autoVendor = new Vendors();
        autoVendor.setCompany(targetCompany);
        autoVendor.setVendorName(vendorName);
        autoVendor.setDisplayName(vendorName);
        autoVendor.setPhone(requesterPhone);
        autoVendor.setEmail(ownerCompany.getContactEmail());
        autoVendor.setConnectionStatus(ConnectionStatus.PENDING);
        Vendors savedVendor = vendorRepository.save(autoVendor);

        // Create the auto-invitation record
        Invitation invitation = new Invitation();
        invitation.setFromCompany(ownerCompany);
        invitation.setToCompany(targetCompany);
        invitation.setStatus(STATUS_PENDING);
        invitation.setSendAt(LocalDateTime.now());
        invitation.setIsActive(true);
        invitation.setRequestedEntityType(TYPE_CUSTOMER);
        invitation.setRequestedEntityId(customer.getCustomerId());
        invitation.setSelectedVendorId(savedVendor.getVendorId());
        invitation.setInvitationCode(generateUniqueInvitationCode());
        invitation.setRequestType(REQUEST_TYPE_AUTO);
        invitation.setRequesterCompanyPhone(requesterPhone);
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        // Send notification to Company B
        notificationService.createCompanyNotification(
                ownerCompany.getCompanyId(),
                targetCompany.getCompanyId(),
                "New Connection Request",
                ownerCompany.getCompanyName() + " wants to connect with you as a vendor",
                "CONNECTION_REQUEST",
                "View Request",
                "/cf/company/" + targetCompany.getCompanyId() + "/vendors/" + savedVendor.getVendorId() + "/detail",
                "CONNECTION_REQUEST",
                "VENDOR",
                savedVendor.getVendorId());

        partnerBalanceService.refreshVendorDueAmount(savedVendor.getVendorId());
        return savedVendor;
    }

    /**
     * Called when Company B creates a Vendor whose phone matches Company A's user.
     * Creates a PENDING connection request and auto-creates a Customer on Company A's side.
     *
     * @param vendor       the saved Vendor record (on Company B's side)
     * @param ownerCompany Company B (the company that created the vendor)
     * @param targetCompany Company A (the matched company)
     * @return the auto-created Customer on Company A's side
     */
    @Transactional
    public Customers createConnectionRequestFromVendor(Vendors vendor, Companies ownerCompany, Companies targetCompany) {
        // Auto-create a Customer on Company A's side with requester's phone as the name
        String requesterPhone = ownerCompany.getContactPhone() != null
                ? ownerCompany.getContactPhone()
                : vendor.getPhone();
        String customerName = requesterPhone != null ? requesterPhone : ownerCompany.getCompanyName();

        Customers autoCustomer = new Customers();
        autoCustomer.setCompany(targetCompany);
        autoCustomer.setCustomerName(customerName);
        autoCustomer.setDisplayName(customerName);
        autoCustomer.setPhone(requesterPhone);
        autoCustomer.setEmail(ownerCompany.getContactEmail());
        autoCustomer.setConnectionStatus(ConnectionStatus.PENDING);
        Customers savedCustomer = customerRepository.save(autoCustomer);

        // Create the auto-invitation record
        Invitation invitation = new Invitation();
        invitation.setFromCompany(ownerCompany);
        invitation.setToCompany(targetCompany);
        invitation.setStatus(STATUS_PENDING);
        invitation.setSendAt(LocalDateTime.now());
        invitation.setIsActive(true);
        invitation.setRequestedEntityType(TYPE_VENDOR);
        invitation.setRequestedEntityId(vendor.getVendorId());
        invitation.setSelectedCustomerId(savedCustomer.getCustomerId());
        invitation.setInvitationCode(generateUniqueInvitationCode());
        invitation.setRequestType(REQUEST_TYPE_AUTO);
        invitation.setRequesterCompanyPhone(requesterPhone);
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        // Send notification to Company A
        notificationService.createCompanyNotification(
                ownerCompany.getCompanyId(),
                targetCompany.getCompanyId(),
                "New Connection Request",
                ownerCompany.getCompanyName() + " wants to connect with you as a customer",
                "CONNECTION_REQUEST",
                "View Request",
                "/cf/company/" + targetCompany.getCompanyId() + "/customers/" + savedCustomer.getCustomerId() + "/detail",
                "CONNECTION_REQUEST",
                "CUSTOMER",
                savedCustomer.getCustomerId());

        partnerBalanceService.refreshCustomerDueAmount(savedCustomer.getCustomerId());
        return savedCustomer;
    }

    /**
     * Accept a connection request for a Customer record.
     * Sets both sides to ACCEPTED and creates the CompanyLink.
     */
    @Transactional
    public void acceptConnectionForCustomer(Long companyId, Long customerId) {
        Customers customer = customerRepository.findByCustomerIdAndCompanyCompanyId(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        if (!ConnectionStatus.isPending(customer.getConnectionStatus())
                && !ConnectionStatus.isRejected(customer.getConnectionStatus())) {
            throw new RuntimeException("Customer connection is not in a state that can be accepted");
        }

        // Find the related auto-invitation
        Invitation invitation = findAutoInvitationForEntity(TYPE_CUSTOMER, customerId)
                .or(() -> findAutoInvitationForEntity(TYPE_VENDOR, null, customerId))
                .orElseThrow(() -> new RuntimeException("Connection request not found for customer"));

        // Determine the two companies and the counterpart entity
        Companies requesterCompany = invitation.getFromCompany();
        Companies receiverCompany = invitation.getToCompany();

        // Set customer side to ACCEPTED
        customer.setConnectionStatus(ConnectionStatus.ACCEPTED);
        customer.setCustomerCompany(
                customer.getCompany().getCompanyId().equals(requesterCompany.getCompanyId())
                        ? receiverCompany : requesterCompany);
        customer.setAcceptedInvitationId(invitation.getInviteId());
        customerRepository.save(customer);

        // Find and update vendor counterpart
        Vendors vendor = resolveVendorFromInvitation(invitation);
        if (vendor != null) {
            vendor.setConnectionStatus(ConnectionStatus.ACCEPTED);
            vendor.setVendorCompany(
                    vendor.getCompany().getCompanyId().equals(requesterCompany.getCompanyId())
                            ? receiverCompany : requesterCompany);
            vendor.setAcceptedInvitationId(invitation.getInviteId());
            vendorRepository.save(vendor);
        }

        // Create CompanyLink
        if (vendor != null) {
            upsertCompanyLink(customer, vendor);
        }

        // Update invitation status
        invitation.setStatus(STATUS_ACCEPTED);
        invitation.setAccespedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        // Send notification to requester
        notificationService.createCompanyNotification(
                companyId,
                requesterCompany.getCompanyId().equals(companyId)
                        ? receiverCompany.getCompanyId() : requesterCompany.getCompanyId(),
                "Connection Accepted",
                "Your connection request has been accepted",
                "CONNECTION_ACCEPTED",
                "View",
                null);
    }

    /**
     * Accept a connection request for a Vendor record.
     */
    @Transactional
    public void acceptConnectionForVendor(Long companyId, Long vendorId) {
        Vendors vendor = vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

        if (!ConnectionStatus.isPending(vendor.getConnectionStatus())
                && !ConnectionStatus.isRejected(vendor.getConnectionStatus())) {
            throw new RuntimeException("Vendor connection is not in a state that can be accepted");
        }

        Invitation invitation = findAutoInvitationForEntity(TYPE_VENDOR, vendorId)
                .or(() -> findAutoInvitationForEntity(TYPE_CUSTOMER, null, vendorId))
                .orElseThrow(() -> new RuntimeException("Connection request not found for vendor"));

        Companies requesterCompany = invitation.getFromCompany();
        Companies receiverCompany = invitation.getToCompany();

        // Set vendor side to ACCEPTED
        vendor.setConnectionStatus(ConnectionStatus.ACCEPTED);
        vendor.setVendorCompany(
                vendor.getCompany().getCompanyId().equals(requesterCompany.getCompanyId())
                        ? receiverCompany : requesterCompany);
        vendor.setAcceptedInvitationId(invitation.getInviteId());
        vendorRepository.save(vendor);

        // Find and update customer counterpart
        Customers customer = resolveCustomerFromInvitation(invitation);
        if (customer != null) {
            customer.setConnectionStatus(ConnectionStatus.ACCEPTED);
            customer.setCustomerCompany(
                    customer.getCompany().getCompanyId().equals(requesterCompany.getCompanyId())
                            ? receiverCompany : requesterCompany);
            customer.setAcceptedInvitationId(invitation.getInviteId());
            customerRepository.save(customer);
        }

        // Create CompanyLink
        if (customer != null) {
            upsertCompanyLink(customer, vendor);
        }

        // Update invitation
        invitation.setStatus(STATUS_ACCEPTED);
        invitation.setAccespedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        // Notify requester
        notificationService.createCompanyNotification(
                companyId,
                requesterCompany.getCompanyId().equals(companyId)
                        ? receiverCompany.getCompanyId() : requesterCompany.getCompanyId(),
                "Connection Accepted",
                "Your connection request has been accepted",
                "CONNECTION_ACCEPTED",
                "View",
                null);
    }

    /**
     * Reject a connection request for a Customer record.
     */
    @Transactional
    public void rejectConnectionForCustomer(Long companyId, Long customerId) {
        Customers customer = customerRepository.findByCustomerIdAndCompanyCompanyId(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        if (!ConnectionStatus.isPending(customer.getConnectionStatus())
                && !ConnectionStatus.isAccepted(customer.getConnectionStatus())) {
            throw new RuntimeException("Customer connection is not in a state that can be rejected");
        }

        boolean wasAccepted = ConnectionStatus.isAccepted(customer.getConnectionStatus());

        Invitation invitation = findAutoInvitationForEntity(TYPE_CUSTOMER, customerId)
                .or(() -> findAutoInvitationForEntity(TYPE_VENDOR, null, customerId))
                .orElseThrow(() -> new RuntimeException("Connection request not found for customer"));

        // Set customer to REJECTED and clear link
        customer.setConnectionStatus(ConnectionStatus.REJECTED);
        customer.setCustomerCompany(null);
        customer.setAcceptedInvitationId(null);
        customerRepository.save(customer);

        // Update vendor counterpart
        Vendors vendor = resolveVendorFromInvitation(invitation);
        if (vendor != null) {
            vendor.setConnectionStatus(ConnectionStatus.REJECTED);
            vendor.setVendorCompany(null);
            vendor.setAcceptedInvitationId(null);
            vendorRepository.save(vendor);
        }

        // Remove CompanyLink if it existed
        if (wasAccepted) {
            deactivateCompanyLink(customer, vendor);
        }

        // Update invitation
        invitation.setStatus(STATUS_REJECTED);
        invitation.setIsActive(false);
        invitation.setUpdatedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        // Notify requester
        Companies requesterCompany = invitation.getFromCompany();
        Companies receiverCompany = invitation.getToCompany();
        notificationService.createCompanyNotification(
                companyId,
                requesterCompany.getCompanyId().equals(companyId)
                        ? receiverCompany.getCompanyId() : requesterCompany.getCompanyId(),
                "Connection Rejected",
                "Your connection request has been rejected",
                "CONNECTION_REJECTED",
                "View",
                null);
    }

    /**
     * Reject a connection request for a Vendor record.
     */
    @Transactional
    public void rejectConnectionForVendor(Long companyId, Long vendorId) {
        Vendors vendor = vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

        if (!ConnectionStatus.isPending(vendor.getConnectionStatus())
                && !ConnectionStatus.isAccepted(vendor.getConnectionStatus())) {
            throw new RuntimeException("Vendor connection is not in a state that can be rejected");
        }

        boolean wasAccepted = ConnectionStatus.isAccepted(vendor.getConnectionStatus());

        Invitation invitation = findAutoInvitationForEntity(TYPE_VENDOR, vendorId)
                .or(() -> findAutoInvitationForEntity(TYPE_CUSTOMER, null, vendorId))
                .orElseThrow(() -> new RuntimeException("Connection request not found for vendor"));

        // Set vendor to REJECTED and clear link
        vendor.setConnectionStatus(ConnectionStatus.REJECTED);
        vendor.setVendorCompany(null);
        vendor.setAcceptedInvitationId(null);
        vendorRepository.save(vendor);

        // Update customer counterpart
        Customers customer = resolveCustomerFromInvitation(invitation);
        if (customer != null) {
            customer.setConnectionStatus(ConnectionStatus.REJECTED);
            customer.setCustomerCompany(null);
            customer.setAcceptedInvitationId(null);
            customerRepository.save(customer);
        }

        // Remove CompanyLink if it existed
        if (wasAccepted) {
            deactivateCompanyLink(customer, vendor);
        }

        // Update invitation
        invitation.setStatus(STATUS_REJECTED);
        invitation.setIsActive(false);
        invitation.setUpdatedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        // Notify requester
        Companies requesterCompany = invitation.getFromCompany();
        Companies receiverCompany = invitation.getToCompany();
        notificationService.createCompanyNotification(
                companyId,
                requesterCompany.getCompanyId().equals(companyId)
                        ? receiverCompany.getCompanyId() : requesterCompany.getCompanyId(),
                "Connection Rejected",
                "Your connection request has been rejected",
                "CONNECTION_REJECTED",
                "View",
                null);
    }

    // --- Helper methods ---

    private Optional<Invitation> findAutoInvitationForEntity(String entityType, Long entityId) {
        return invitationRepository
                .findTopByRequestedEntityTypeAndRequestedEntityIdAndRequestTypeAndIsActiveTrueOrderByCreatedAtDesc(
                        entityType, entityId, REQUEST_TYPE_AUTO);
    }

    /**
     * Find auto-invitation where the entity is on the counterpart side
     * (selectedCustomerId or selectedVendorId).
     */
    private Optional<Invitation> findAutoInvitationForEntity(String entityType, Long customerId, Long vendorId) {
        // Search by selectedCustomerId or selectedVendorId depending on type
        // Fall back to searching all AUTO invitations and matching
        if (TYPE_CUSTOMER.equals(entityType) && vendorId != null) {
            // Looking for invitation where selectedVendorId = vendorId
            return invitationRepository.findAll().stream()
                    .filter(inv -> REQUEST_TYPE_AUTO.equals(inv.getRequestType()))
                    .filter(inv -> vendorId.equals(inv.getSelectedVendorId()))
                    .findFirst();
        } else if (TYPE_VENDOR.equals(entityType) && customerId != null) {
            // Looking for invitation where selectedCustomerId = customerId
            return invitationRepository.findAll().stream()
                    .filter(inv -> REQUEST_TYPE_AUTO.equals(inv.getRequestType()))
                    .filter(inv -> customerId.equals(inv.getSelectedCustomerId()))
                    .findFirst();
        }
        return Optional.empty();
    }

    private Vendors resolveVendorFromInvitation(Invitation invitation) {
        if (invitation.getSelectedVendorId() != null) {
            return vendorRepository.findById(invitation.getSelectedVendorId()).orElse(null);
        }
        // If the invitation was for a vendor, the requestedEntityId IS the vendor
        if (TYPE_VENDOR.equals(invitation.getRequestedEntityType())) {
            return vendorRepository.findById(invitation.getRequestedEntityId()).orElse(null);
        }
        return null;
    }

    private Customers resolveCustomerFromInvitation(Invitation invitation) {
        if (invitation.getSelectedCustomerId() != null) {
            return customerRepository.findById(invitation.getSelectedCustomerId()).orElse(null);
        }
        // If the invitation was for a customer, the requestedEntityId IS the customer
        if (TYPE_CUSTOMER.equals(invitation.getRequestedEntityType())) {
            return customerRepository.findById(invitation.getRequestedEntityId()).orElse(null);
        }
        return null;
    }

    private void upsertCompanyLink(Customers customer, Vendors vendor) {
        if (customer == null || vendor == null) return;

        CompanyLink link = companyLinkRepository.findByCustomerCustomerId(customer.getCustomerId())
                .or(() -> companyLinkRepository.findByVendorVendorId(vendor.getVendorId()))
                .orElseGet(CompanyLink::new);

        link.setCustomer(customer);
        link.setVendor(vendor);
        link.setCustomerCompany(vendor.getCompany());
        link.setVendorCompany(customer.getCompany());
        link.setIsActive(true);
        companyLinkRepository.save(link);
    }

    private void deactivateCompanyLink(Customers customer, Vendors vendor) {
        if (customer != null) {
            companyLinkRepository.findByCustomerCustomerId(customer.getCustomerId())
                    .ifPresent(link -> {
                        link.setIsActive(false);
                        companyLinkRepository.save(link);
                    });
        }
        if (vendor != null) {
            companyLinkRepository.findByVendorVendorId(vendor.getVendorId())
                    .ifPresent(link -> {
                        link.setIsActive(false);
                        companyLinkRepository.save(link);
                    });
        }
    }

    private String generateUniqueInvitationCode() {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            StringBuilder code = new StringBuilder(INVITATION_CODE_LENGTH);
            for (int i = 0; i < INVITATION_CODE_LENGTH; i++) {
                int idx = RANDOM.nextInt(INVITATION_CODE_ALPHABET.length());
                code.append(INVITATION_CODE_ALPHABET.charAt(idx));
            }
            String codeStr = code.toString();
            if (!invitationRepository.existsByInvitationCode(codeStr)) {
                return codeStr;
            }
        }
        throw new RuntimeException("Failed to generate unique invitation code. Please try again.");
    }
}
