package com.astraval.coreflow.modules.userrolemap;

import lombok.Data;
import java.time.LocalDateTime;

import com.astraval.coreflow.modules.role.Role;
import com.astraval.coreflow.modules.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "user_role_map")
@Data
public class UserRoleMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_role_map_id")
    private Integer userRoleMapId;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, insertable = false, updatable = false)
    private Role role;

    // default fields...
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by", length = 100, nullable = false)
    private String createdBy;

    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    @Column(name = "modified_dt")
    private LocalDateTime modifiedDt;


}
