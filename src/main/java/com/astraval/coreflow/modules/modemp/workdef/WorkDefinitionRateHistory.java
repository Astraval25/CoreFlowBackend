package com.astraval.coreflow.modules.modemp.workdef;

import com.astraval.coreflow.modules.modemp.enums.WorkUnit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_definition_rate_history", schema = "modemp")
public class WorkDefinitionRateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_history_id")
    private Long rateHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_def_id", nullable = false)
    private WorkDefinition workDefinition;

    @Column(name = "rate_per_unit", precision = 38, scale = 2, nullable = false)
    private BigDecimal ratePerUnit;

    @Column(name = "unit", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkUnit unit;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "changed_dt")
    private LocalDateTime changedDt;
}
