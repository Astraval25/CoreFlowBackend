package com.astraval.coreflow.modules.companylink;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.vendor.Vendors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bridges two companies in a B2B relationship after an invitation is accepted.
 *
 * Scenario: Company A (seller) has a Customers record for Company B,
 *           and Company B (buyer) has a Vendors record for Company A.
 *           This table links those two records together.
 *
 * Column mapping (counter-intuitive naming — read carefully):
 *   customer        -> the Customers record (owned by the seller, Company A)
 *   customerCompany -> the BUYER's company (Company B) — i.e. the company the customer represents
 *   vendor          -> the Vendors record (owned by the buyer, Company B)
 *   vendorCompany   -> the SELLER's company (Company A) — i.e. the company the vendor represents
 *
 * Created by InvitationService.upsertCustomerVendorLink() when an invitation is accepted.
 * Unique constraint on customer_id — one customer maps to at most one vendor.
 *
 * Used throughout the app to:
 *   - Resolve the counterpart record (customer <-> vendor) for single-source orders/payments
 *   - Determine linked items in VendorItemService (linked vendor shows ItemCustomerPrice from the other company)
 *   - Populate both sides of OrderDetails and Payments when creating cross-company transactions
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_link", uniqueConstraints = @UniqueConstraint(columnNames = "customer_id"))
@EntityListeners(AuditingEntityListener.class)
public class CompanyLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_link_id")
    private Long CompanyLinkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customers customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_company", nullable = false)
    private Companies customerCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendors vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_company", nullable = false)
    private Companies vendorCompany;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @LastModifiedDate
    @Column(name = "modified_dt")
    private LocalDateTime modifiedDt;
}
