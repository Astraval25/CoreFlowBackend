package com.astraval.coreflow.modules.userrolemap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleMapRepository extends JpaRepository<UserRoleMap, Integer> {
    
    @Query("SELECT urm FROM UserRoleMap urm " +
           "WHERE urm.user.userId = :userId AND urm.isActive = true")
    Optional<UserRoleMap> findByUserIdAndIsActiveTrue(@Param("userId") Integer userId);
}