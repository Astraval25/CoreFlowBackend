package com.astraval.coreflow.main_modules.usercompmap;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCompanyMapRepository extends JpaRepository<UserCompanyMap, Integer> {

    Optional<UserCompanyMap> findByUserUserIdAndCompanyCompanyId(Long userId, Long companyId);

    List<UserCompanyMap> findByCompanyCompanyIdAndIsActiveTrue(Long companyId);

    @Query("SELECT ucm.user.userId FROM UserCompanyMap ucm WHERE ucm.company.companyId = :companyId AND ucm.isActive = true")
    List<Long> findUserIdsByCompanyId(@Param("companyId") Long companyId);
}