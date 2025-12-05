package com.astraval.coreflow.model;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "tblcompanies")
public class Companies {
    @Id
    @Column(name = "company_id" , length = 36, nullable = false)
    private String companyId;

    @Column(name = "company_name" , length = 500, nullable = false)
    private String companyname;

    @Column(name = "industry" , length = 250, nullable = false)
    private String industry;

    @Column(name = "pan" , length = 50, nullable = false)
    private String pan;

    @Column(name = "gst_no" , length = 50, nullable = false)
    private String gstNo;

    @Column(name = "hsn_code" , length = 50, nullable = false)
    private String hsnCode;

    @Column(name = "short_name" , length = 200)
    private String shortName;

    // defule fields

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by", length = 100, nullable = false)
    private String createdBy;

    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @Column(name = "modifed_by", length = 100, nullable = true)
    private String modifiedBy;

    @Column(name = "modified_dt", nullable = true)
    private LocalDateTime modifiedDt;

    
}
