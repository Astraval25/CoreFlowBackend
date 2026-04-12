package com.astraval.coreflow.employee_module.salary;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.astraval.coreflow.employee_module.employee.Employee;
import com.astraval.coreflow.employee_module.enums.SalaryPeriodStatus;
import com.astraval.coreflow.employee_module.enums.SalaryType;
import com.astraval.coreflow.main_modules.companies.Companies;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_salary_periods", schema = "modemp",
                uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "from_date", "to_date"}))
public class EmployeeSalaryPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salary_period_id")
    private Long salaryPeriodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @Column(name = "period", length = 6, nullable = false)
    private String period;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "salary_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private SalaryType salaryType;

    @Column(name = "working_days_in_month")
    private Integer workingDaysInMonth;

    @Column(name = "days_present", precision = 5, scale = 1)
    private BigDecimal daysPresent;

    @Column(name = "days_absent", precision = 5, scale = 1)
    private BigDecimal daysAbsent;

    @Column(name = "lop_days", precision = 5, scale = 1)
    private BigDecimal lopDays;

    @Column(name = "gross_amount", precision = 38, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "lop_deduction", precision = 38, scale = 2)
    private BigDecimal lopDeduction;

    @Column(name = "other_deductions", precision = 38, scale = 2)
    private BigDecimal otherDeductions;

    @Column(name = "net_amount", precision = 38, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private SalaryPeriodStatus status = SalaryPeriodStatus.DRAFT;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_dt")
    private LocalDateTime approvedDt;

    @Column(name = "paid_dt")
    private LocalDateTime paidDt;

    @Column(name = "payment_ref", length = 100)
    private String paymentRef;

    @Column(name = "computed_dt")
    private LocalDateTime computedDt;

    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @PrePersist
    protected void onCreate() {
        createdDt = LocalDateTime.now();
    }
}
