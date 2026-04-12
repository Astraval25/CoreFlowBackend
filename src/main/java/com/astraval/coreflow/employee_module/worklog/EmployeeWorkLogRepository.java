package com.astraval.coreflow.employee_module.worklog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.employee_module.enums.WorkLogStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeWorkLogRepository extends JpaRepository<EmployeeWorkLog, Long> {

    List<EmployeeWorkLog> findByCompanyCompanyIdAndLogDateBetweenOrderByLogDateDesc(
            Long companyId, LocalDate from, LocalDate to);

    List<EmployeeWorkLog> findByEmployeeEmployeeIdAndLogDateBetweenOrderByLogDateDesc(
            Long employeeId, LocalDate from, LocalDate to);

    List<EmployeeWorkLog> findByCompanyCompanyIdAndStatusOrderByLogDateDesc(
            Long companyId, WorkLogStatus status);

    Optional<EmployeeWorkLog> findByLogIdAndCompanyCompanyId(Long logId, Long companyId);

    List<EmployeeWorkLog> findByEmployeeEmployeeIdAndCompanyCompanyIdAndWorkDefinitionWorkDefIdAndLogDate(
            Long employeeId, Long companyId, Long workDefId, LocalDate logDate);

    @Query("SELECT w FROM EmployeeWorkLog w " +
            "WHERE w.employee.employeeId = :employeeId " +
            "AND w.status = 'APPROVED' " +
            "AND w.logDate BETWEEN :startDate AND :endDate")
    List<EmployeeWorkLog> findApprovedLogsByEmployeeAndPeriod(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
