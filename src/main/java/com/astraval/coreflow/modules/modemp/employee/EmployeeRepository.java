package com.astraval.coreflow.modules.modemp.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByCompanyCompanyIdAndIsActiveTrueOrderByEmployeeName(Long companyId);

    List<Employee> findByCompanyCompanyIdOrderByEmployeeName(Long companyId);

    Optional<Employee> findByEmployeeIdAndCompanyCompanyId(Long employeeId, Long companyId);

    boolean existsByCompanyCompanyIdAndEmployeeCode(Long companyId, String employeeCode);
}
