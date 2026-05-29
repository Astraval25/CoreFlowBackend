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

    @Query(value = """
            SELECT *
            FROM users u
            WHERE u.is_active = TRUE
              AND LENGTH(REGEXP_REPLACE(COALESCE(u.contact_no, ''), '[^0-9]', '', 'g')) >= 10
              AND RIGHT(REGEXP_REPLACE(COALESCE(u.contact_no, ''), '[^0-9]', '', 'g'), 10) = :phoneKey
            ORDER BY u.user_id
            LIMIT 1
            """, nativeQuery = true)
    Optional<User> findActiveUserByPhoneKey(@Param("phoneKey") String phoneKey);
    
    Optional<User> findByUserNameAndIsActiveTrue(@Param("userName") String userName);

}
