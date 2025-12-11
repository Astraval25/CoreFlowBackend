package com.astraval.coreflow.modules.usercompmap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCompanyMapRepository extends JpaRepository<UserCompanyMap, Integer>{

    List<UserCompanyMap> findByUserUserId(Integer userId);

}
