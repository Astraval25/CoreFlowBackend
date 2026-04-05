package com.astraval.coreflow.modules.modemp.salary;

import com.astraval.coreflow.modules.modemp.enums.SalaryLineType;
import com.astraval.coreflow.modules.modemp.enums.WorkUnit;
import com.astraval.coreflow.modules.modemp.workdef.WorkDefinition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_salary_lines", schema = "modemp")
public class EmployeeSalaryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long lineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_period_id", nullable = false)
    private EmployeeSalaryPeriod salaryPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_def_id")
    private WorkDefinition workDefinition;

    @Column(name = "line_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private SalaryLineType lineType;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "total_qty", precision = 38, scale = 2)
    private BigDecimal totalQty;

    @Column(name = "unit", length = 20)
    @Enumerated(EnumType.STRING)
    private WorkUnit unit;

    @Column(name = "rate_used", precision = 38, scale = 2)
    private BigDecimal rateUsed;

    @Column(name = "amount", precision = 38, scale = 2, nullable = false)
    private BigDecimal amount;
}
