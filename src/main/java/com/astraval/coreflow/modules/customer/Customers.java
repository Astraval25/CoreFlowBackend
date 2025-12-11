package com.astraval.coreflow.modules.customer;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.astraval.coreflow.modules.companies.Companies;

@Entity
@Table(name = "customers")
@Data
public class Customers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comp_id", nullable = false)
    private Companies company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_comp_id")
    private Companies customerCompany;

    @Column(name = "accepted_invitation_id")
    private String acceptedInvitationId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "lang")
    private String lang;

    @Column(name = "pan")
    private String pan;

    @Column(name = "gst")
    private String gst;

    @Column(name = "advance_amount")
    private BigDecimal advanceAmount;

    @Column(name = "billing_addr_id")
    private String billingAddrId;

    @Column(name = "shipping_addr_id")
    private String shippingAddrId;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "update_at")
    private Long updateAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}