package com.astraval.coreflow.modules.modemp.salary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSalaryPeriodRepository extends JpaRepository<EmployeeSalaryPeriod, Long> {

    List<EmployeeSalaryPeriod> findByCompanyCompanyIdAndPeriodOrderByEmployeeEmployeeName(
            Long companyId, String period);

    Optional<EmployeeSalaryPeriod> findByEmployeeEmployeeIdAndFromDateAndToDate(
            Long employeeId, LocalDate fromDate, LocalDate toDate);

    Optional<EmployeeSalaryPeriod> findBySalaryPeriodIdAndCompanyCompanyId(
            Long salaryPeriodId, Long companyId);

    List<EmployeeSalaryPeriod> findByCompanyCompanyIdAndFromDateGreaterThanEqualAndToDateLessThanEqualOrderByEmployeeEmployeeName(
            Long companyId, LocalDate fromDate, LocalDate toDate);

    @Query("SELECT sp FROM EmployeeSalaryPeriod sp " +
            "WHERE sp.employee.employeeId = :employeeId " +
            "AND sp.fromDate <= :toDate " +
            "AND sp.toDate >= :fromDate")
    List<EmployeeSalaryPeriod> findOverlappingPeriods(
            @Param("employeeId") Long employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
