package com.astraval.coreflow.employee_module.leavelog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.astraval.coreflow.employee_module.employee.Employee;
import com.astraval.coreflow.employee_module.enums.LeaveCategory;
import com.astraval.coreflow.employee_module.enums.LeaveStatus;
import com.astraval.coreflow.employee_module.enums.LeaveType;
import com.astraval.coreflow.main_modules.companies.Companies;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_leave_logs", schema = "modemp",
                uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "leave_date"}))
public class EmployeeLeaveLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_id")
    private Long leaveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    @Column(name = "leave_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    @Column(name = "leave_category", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveCategory leaveCategory;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_dt")
    private LocalDateTime approvedDt;

    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @PrePersist
    protected void onCreate() {
        createdDt = LocalDateTime.now();
    }
}
