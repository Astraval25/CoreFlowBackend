package com.astraval.coreflow.employee_module.salary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeSalaryLineRepository extends JpaRepository<EmployeeSalaryLine, Long> {

    List<EmployeeSalaryLine> findBySalaryPeriodSalaryPeriodId(Long salaryPeriodId);

    void deleteBySalaryPeriodSalaryPeriodId(Long salaryPeriodId);
}
