package com.astraval.coreflow.main_modules.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "company_number_sequence",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "number_type", "period"}))
public class CompanyNumberSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_number_sequence_id")
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "number_type", nullable = false, length = 20)
    private String numberType;

    @Column(name = "period", nullable = false, length = 6)
    private String period;

    @Column(name = "last_value", nullable = false)
    private Long lastValue;
}
