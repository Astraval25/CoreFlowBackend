package com.astraval.coreflow.modules.usercompmap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCompanyAssetsRepository extends JpaRepository<UserCompanyAssets, Long> {
    
    @Query("SELECT u FROM UserCompanyAssets u WHERE u.companyId = :companyId")
    UserCompanyAssets findByCompanyId(@Param("companyId") Long companyId);
}