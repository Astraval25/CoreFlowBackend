package com.astraval.coreflow.modules.usercompmap;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCompanyMapRepository extends JpaRepository<UserCompanyMap, Integer> {
    
    Optional<UserCompanyMap> findByUserUserIdAndCompanyCompanyId(Long userId, Long companyId);
}