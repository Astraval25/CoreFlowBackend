package com.astraval.coreflow.main_modules.companyref;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.orderdetails.OrderDetails;

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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_order_ref",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "order_id"}))
@EntityListeners(AuditingEntityListener.class)
public class CompanyOrderRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_order_ref_id")
    private Long companyOrderRefId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderDetails orderDetails;

    @Column(name = "local_order_number", length = 50)
    private String localOrderNumber;

    @Column(name = "internal_remarks", columnDefinition = "TEXT")
    private String internalRemarks;

    @Column(name = "internal_status", length = 50)
    private String internalStatus;

    @Column(name = "internal_tags", length = 255)
    private String internalTags;

    @Column(name = "custom_reference", length = 100)
    private String customReference;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @LastModifiedBy
    @Column(name = "modified_by")
    private Long lastModifiedBy;

    @LastModifiedDate
    @Column(name = "modified_dt")
    private LocalDateTime modifiedDt;
}
