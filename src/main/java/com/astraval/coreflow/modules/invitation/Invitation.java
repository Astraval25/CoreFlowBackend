package com.astraval.coreflow.modules.invitation;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

import com.astraval.coreflow.modules.companies.Companies;

@Entity
@Table(name = "invitation")
@Data
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_id")
    private Long inviteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_company_id")
    private Companies fromCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_comp_id")
    private Companies toCompany;

    @Column(name = "email")
    private String email;

    @Column(name = "status")
    private String status;

    @Column(name = "send_at")
    private LocalDateTime sendAt;

    @Column(name = "accesped_at")
    private LocalDateTime accespedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "invitation_code", nullable = false, unique = true)
    private UUID invitationCode;

    @Column(name = "requested_entity_type")
    private String requestedEntityType; // CUSTOMER or VENDOR

    @Column(name = "requested_entity_id")
    private Long requestedEntityId;

    @Column(name = "selected_customer_id")
    private Long selectedCustomerId;

    @Column(name = "selected_vendor_id")
    private Long selectedVendorId;

    @PrePersist
    public void generateInvitationCode() {
        if (invitationCode == null) {
            invitationCode = UUID.randomUUID();
        }
    }
}
