package com.astraval.coreflow.main_modules.companies;


import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "companies")
public class Companies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "company_name" , length = 500, nullable = false)
    private String companyName;

    @Column(name = "industry" , length = 250, nullable = false)
    private String industry;

    @Column(name = "pan" , length = 50, nullable = true)
    private String pan;

    @Column(name = "gst_no" , length = 50, nullable = true)
    private String gstNo;

    @Column(name = "hsn_code" , length = 50, nullable = true)
    private String hsnCode;

    @Column(name = "short_name" , length = 200)
    private String shortName;

    @Column(name = "fs_id", length = 100)
    private String fsId;

    @Column(name = "contact_person", length = 250)
    private String contactPerson;

    @Column(name = "contact_email", length = 250)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "website", length = 500)
    private String website;

    @Column(name = "address_line1", length = 500)
    private String addressLine1;

    @Column(name = "address_line2", length = 500)
    private String addressLine2;

    @Column(name = "city", length = 150)
    private String city;

    @Column(name = "state", length = 150)
    private String state;

    @Column(name = "country", length = 150)
    private String country;

    @Column(name = "postal_code", length = 40)
    private String postalCode;

    @Column(name = "public_description", length = 2000)
    private String publicDescription;


    // default fields...
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
