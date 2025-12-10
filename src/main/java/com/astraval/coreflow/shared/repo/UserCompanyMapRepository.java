package com.astraval.coreflow.shared.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.astraval.coreflow.shared.model.UserCompanyMap;

import java.util.List;

public interface UserCompanyMapRepository extends JpaRepository<UserCompanyMap, String>{

    List<UserCompanyMap> findByUserUserId(String userId);

}
