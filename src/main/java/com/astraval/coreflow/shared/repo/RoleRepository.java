package com.astraval.coreflow.shared.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.shared.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByRoleCodeAndIsActiveTrue(String roleCode);
}