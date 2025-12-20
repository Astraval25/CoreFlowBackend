package com.astraval.coreflow.modules.userrolemap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleMapRepository extends JpaRepository<UserRoleMap, Integer> {
    
    @Query("SELECT urm FROM UserRoleMap urm JOIN FETCH urm.role WHERE urm.userId = :userId AND urm.isActive = true")
    List<UserRoleMap> findActiveRolesByUserId(@Param("userId") Integer userId);
}