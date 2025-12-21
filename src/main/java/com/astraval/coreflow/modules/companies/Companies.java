package com.astraval.coreflow.modules.companies;


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
    private long companyId;

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
