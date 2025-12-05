package com.astraval.coreflow.repo;

import com.astraval.coreflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    // @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    // User findByEmailAndIsActiveTrue(@Param("email") String email);
    User findByEmailAndIsActiveTrue(@Param("email") String email);

}