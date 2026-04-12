package com.astraval.coreflow.employee_module.salary;

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

    List<EmployeeSalaryPeriod> findByCompanyCompanyIdAndEmployeeEmployeeIdAndPeriodOrderByFromDateDesc(
            Long companyId, Long employeeId, String period);

    Optional<EmployeeSalaryPeriod> findByEmployeeEmployeeIdAndFromDateAndToDate(
            Long employeeId, LocalDate fromDate, LocalDate toDate);

    Optional<EmployeeSalaryPeriod> findBySalaryPeriodIdAndCompanyCompanyId(
            Long salaryPeriodId, Long companyId);

    List<EmployeeSalaryPeriod> findByCompanyCompanyIdAndFromDateGreaterThanEqualAndToDateLessThanEqualOrderByEmployeeEmployeeName(
            Long companyId, LocalDate fromDate, LocalDate toDate);

    List<EmployeeSalaryPeriod> findByCompanyCompanyIdAndEmployeeEmployeeIdAndFromDateGreaterThanEqualAndToDateLessThanEqualOrderByFromDateDesc(
            Long companyId, Long employeeId, LocalDate fromDate, LocalDate toDate);

    @Query("SELECT sp FROM EmployeeSalaryPeriod sp " +
            "WHERE sp.employee.employeeId = :employeeId " +
            "AND sp.fromDate <= :toDate " +
            "AND sp.toDate >= :fromDate")
    List<EmployeeSalaryPeriod> findOverlappingPeriods(
            @Param("employeeId") Long employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END " +
            "FROM EmployeeSalaryPeriod sp " +
            "WHERE sp.company.companyId = :companyId " +
            "AND sp.employee.employeeId = :employeeId " +
            "AND :targetDate BETWEEN sp.fromDate AND sp.toDate")
    boolean existsSalaryPeriodForEmployeeOnDate(
            @Param("companyId") Long companyId,
            @Param("employeeId") Long employeeId,
            @Param("targetDate") LocalDate targetDate);
}
