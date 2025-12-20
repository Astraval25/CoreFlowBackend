package com.astraval.coreflow.modules.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.user.dto.AuthRow;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndIsActiveTrue(@Param("email") String email);

    Optional<User> findByUserNameAndIsActiveTrue(@Param("userName") String userName);

    // @Query("""
    //           SELECT new com.astraval.coreflow.modules.user.dto.AuthRow(
    //               u.email
    //           )
    //           FROM User u
    //           WHERE u.email = :email
    //             AND u.isActive = true
    //         """)
    // List<AuthRow> findAuthDataByEmail(String email);
    
    @Query("""
                SELECT u
                FROM User u
                WHERE u.email = :email
                  AND u.isActive = true
            """)
    Optional<User> findActiveUserByEmail(@Param("email") String email);

}