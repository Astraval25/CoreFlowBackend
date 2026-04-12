package com.astraval.coreflow.employee_module.portaluser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeePortalUserRepository extends JpaRepository<EmployeePortalUser, Long> {

    Optional<EmployeePortalUser> findByEmployeeEmployeeId(Long employeeId);

    Optional<EmployeePortalUser> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<EmployeePortalUser> findByUsernameAndCompanyCompanyId(String username, Long companyId);
}
