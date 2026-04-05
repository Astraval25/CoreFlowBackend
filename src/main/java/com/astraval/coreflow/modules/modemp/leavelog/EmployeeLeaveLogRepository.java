package com.astraval.coreflow.modules.modemp.leavelog;

import com.astraval.coreflow.modules.modemp.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeLeaveLogRepository extends JpaRepository<EmployeeLeaveLog, Long> {

    List<EmployeeLeaveLog> findByCompanyCompanyIdAndLeaveDateBetweenOrderByLeaveDateDesc(
            Long companyId, LocalDate from, LocalDate to);

    List<EmployeeLeaveLog> findByEmployeeEmployeeIdAndLeaveDateBetweenOrderByLeaveDateDesc(
            Long employeeId, LocalDate from, LocalDate to);

    List<EmployeeLeaveLog> findByCompanyCompanyIdAndStatusOrderByLeaveDateDesc(
            Long companyId, LeaveStatus status);

    Optional<EmployeeLeaveLog> findByLeaveIdAndCompanyCompanyId(Long leaveId, Long companyId);

    boolean existsByEmployeeEmployeeIdAndCompanyCompanyIdAndLeaveDate(
            Long employeeId, Long companyId, LocalDate leaveDate);

    @Query("SELECT l FROM EmployeeLeaveLog l " +
            "WHERE l.employee.employeeId = :employeeId " +
            "AND l.status = 'APPROVED' " +
            "AND l.leaveDate BETWEEN :startDate AND :endDate")
    List<EmployeeLeaveLog> findApprovedLeavesByEmployeeAndPeriod(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
