package com.astraval.coreflow.employee_module.salaryconfig;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.astraval.coreflow.employee_module.employee.Employee;
import com.astraval.coreflow.employee_module.enums.SalaryType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_salary_config", schema = "modemp",
                indexes = @Index(columnList = "employee_id, effective_from"))
@EntityListeners(AuditingEntityListener.class)
public class EmployeeSalaryConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "salary_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private SalaryType salaryType;

    @Column(name = "monthly_amount", precision = 38, scale = 2)
    private BigDecimal monthlyAmount;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @CreatedBy
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;
}
