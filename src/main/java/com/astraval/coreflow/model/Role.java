package com.astraval.coreflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Table(name = "tblroles")
@Data
public class Role {

    @Id
    @Column(name="role_id", length = 36)
    private String roleId;

    @Column(name = "role_code", length = 5)
    private String roleCode;

    @Column(name = "role_name", length = 100, nullable = false)
    private String roleName;

    @Column(name = "landing_url", length = 200)
    private String landingUrl;

    // defule fields
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by", length = 100, nullable = false)
    private String createdBy;

    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @Column(name = "modifed_by", length = 100, nullable = true)
    private String modifiedBy;

    @Column(name = "modified_dt", nullable = true)
    private LocalDateTime modifiedDt;

    // @OneToMany(mappedBy = "role")
    // private List<UserRoleMap> mappedUsers;
}
