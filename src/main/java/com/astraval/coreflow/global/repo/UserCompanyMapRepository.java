package com.astraval.coreflow.global.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.astraval.coreflow.global.model.UserCompanyMap;

import java.util.List;

public interface UserCompanyMapRepository extends JpaRepository<UserCompanyMap, String>{

    List<UserCompanyMap> findByUserUserId(String userId);

}
