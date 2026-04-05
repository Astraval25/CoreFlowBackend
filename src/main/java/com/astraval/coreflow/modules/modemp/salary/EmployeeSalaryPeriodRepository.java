package com.astraval.coreflow.modules.modemp.salary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSalaryPeriodRepository extends JpaRepository<EmployeeSalaryPeriod, Long> {

    List<EmployeeSalaryPeriod> findByCompanyCompanyIdAndPeriodOrderByEmployeeEmployeeName(
            Long companyId, String period);

    Optional<EmployeeSalaryPeriod> findByEmployeeEmployeeIdAndPeriod(Long employeeId, String period);

    Optional<EmployeeSalaryPeriod> findBySalaryPeriodIdAndCompanyCompanyId(
            Long salaryPeriodId, Long companyId);
}
