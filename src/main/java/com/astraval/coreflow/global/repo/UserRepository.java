package com.astraval.coreflow.global.repo;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.global.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    // @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    // User findByEmailAndIsActiveTrue(@Param("email") String email);
    User findByEmailAndIsActiveTrue(@Param("email") String email);
    User findByUserNameAndIsActiveTrue(@Param("userName") String userName);

}