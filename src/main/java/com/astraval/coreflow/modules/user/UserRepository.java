package com.astraval.coreflow.modules.user;

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
            """)
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    Optional<User> findByUserNameAndIsActiveTrue(@Param("userName") String userName);

}