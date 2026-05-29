package com.astraval.coreflow.main_modules.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("""
                SELECT u
                FROM User u
        WHERE u.email = :email
          AND u.isActive = true
            """)
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    @Query("""
                SELECT u
                FROM User u
        WHERE u.contactNo = :contactNo
          AND u.isActive = true
            """)
    Optional<User> findActiveUserByContactNo(@Param("contactNo") String contactNo);
    
    Optional<User> findByUserNameAndIsActiveTrue(@Param("userName") String userName);

}
