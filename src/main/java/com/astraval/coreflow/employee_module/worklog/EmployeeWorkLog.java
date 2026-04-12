package com.astraval.coreflow.employee_module.worklog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.astraval.coreflow.employee_module.employee.Employee;
import com.astraval.coreflow.employee_module.enums.WorkLogStatus;
import com.astraval.coreflow.employee_module.enums.WorkUnit;
import com.astraval.coreflow.employee_module.workdef.WorkDefinition;
import com.astraval.coreflow.main_modules.companies.Companies;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_work_logs", schema = "modemp",
                indexes = {
                @Index(columnList = "employee_id, log_date"),
                @Index(columnList = "company_id, log_date, status")
        })
public class EmployeeWorkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_def_id", nullable = false)
    private WorkDefinition workDefinition;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "quantity", precision = 38, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkUnit unit;

    @Column(name = "rate_snapshot", precision = 38, scale = 2, nullable = false)
    private BigDecimal rateSnapshot;

    @Column(name = "amount_earned", precision = 38, scale = 2, nullable = false)
    private BigDecimal amountEarned;

    @Column(name = "employee_remarks", columnDefinition = "TEXT")
    private String employeeRemarks;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkLogStatus status = WorkLogStatus.PENDING;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_dt")
    private LocalDateTime reviewedDt;

    @Column(name = "admin_remarks", columnDefinition = "TEXT")
    private String adminRemarks;

    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @Column(name = "modified_dt")
    private LocalDateTime modifiedDt;

    @PrePersist
    protected void onCreate() {
        createdDt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDt = LocalDateTime.now();
    }
}
