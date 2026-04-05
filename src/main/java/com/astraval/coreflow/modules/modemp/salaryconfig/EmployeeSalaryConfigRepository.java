package com.astraval.coreflow.modules.modemp.salaryconfig;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSalaryConfigRepository extends JpaRepository<EmployeeSalaryConfig, Long> {

    Optional<EmployeeSalaryConfig> findByEmployeeEmployeeIdAndEffectiveToIsNull(Long employeeId);

    List<EmployeeSalaryConfig> findByEmployeeEmployeeIdOrderByEffectiveFromDesc(Long employeeId);
}
