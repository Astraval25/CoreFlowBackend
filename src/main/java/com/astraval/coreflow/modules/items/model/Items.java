package com.astraval.coreflow.modules.items.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.items.ItemType;
import com.astraval.coreflow.modules.items.UnitType;
import com.astraval.coreflow.modules.vendor.Vendors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
@EntityListeners(AuditingEntityListener.class)
public class Items {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comp_id", nullable = false)
    private Companies company;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_display_name", nullable = false)
    private String itemDisplayName;

    @Column(name = "item_type")
    @Enumerated(EnumType.STRING)
    private ItemType itemType; // GOODS / SERVICE

    @Column(name = "unit")
    @Enumerated(EnumType.STRING)
    private UnitType unit; // KG / ML / PCS

    @Column(name = "sales_description")
    private String salesDescription;

    @Column(name = "sales_price")
    private BigDecimal salesPrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_customer_id")
    private Customers preferredCustomer;

    @Column(name = "purchase_description")
    private String purchaseDescription;

    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_vendor_id")
    private Vendors preferredVendor;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(name = "tax_rate")
    private BigDecimal taxRate;

    @Column(name = "fs_id")
    private String fsId;

    // Default flags and audit fields
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedBy
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @LastModifiedBy
    @Column(name = "modified_by")
    private Long lastModifiedBy;

    @LastModifiedDate
    @Column(name = "modified_dt")
    private LocalDateTime lastModifiedDt;
}
