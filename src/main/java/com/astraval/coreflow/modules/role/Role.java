package com.astraval.coreflow.modules.role;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "roles")
@Getter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="role_id")
    private Integer roleId;

    @Column(name = "role_code", length = 5)
    private String roleCode;

    @Column(name = "role_name", length = 100, nullable = false)
    private String roleName;

    @Column(name = "landing_url", length = 200)
    private String landingUrl;
}
