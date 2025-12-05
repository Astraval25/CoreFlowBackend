package com.astraval.coreflow.model;

import lombok.Data;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "tbluserrolemap")
@Data
public class UserRoleMap {

    @Id
    @Column(name="user_role_map_id", length = 36)
    private String userRoleMapId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "role_id", length = 36, nullable = false)
    private String roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, insertable = false, updatable = false)
    private Role role;

    // defaule fields...
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by", length = 100, nullable = false)
    private String createdBy;

    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @Column(name = "modifed_by", length = 100)
    private String modifiedBy;

    @Column(name = "modified_dt")
    private LocalDateTime modifiedDt;


}
