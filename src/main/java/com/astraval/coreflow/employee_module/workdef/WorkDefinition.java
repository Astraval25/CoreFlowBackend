package com.astraval.coreflow.employee_module.workdef;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.astraval.coreflow.employee_module.enums.WorkUnit;
import com.astraval.coreflow.main_modules.companies.Companies;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_definitions", schema = "modemp",
                uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "work_code"}))
@EntityListeners(AuditingEntityListener.class)
public class WorkDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_def_id")
    private Long workDefId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @Column(name = "work_name", length = 255, nullable = false)
    private String workName;

    @Column(name = "work_code", length = 50, nullable = false)
    private String workCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "rate_per_unit", precision = 38, scale = 2, nullable = false)
    private BigDecimal ratePerUnit;

    @Column(name = "unit", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkUnit unit;

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
