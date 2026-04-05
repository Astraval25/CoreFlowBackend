package com.astraval.coreflow.main_modules.role;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "roles_master")
@Getter
public class Role {

    @Id
    @Column(name = "role_code", length = 50)
    private String roleCode;

    @Column(name = "role_name", length = 100, nullable = false)
    private String roleName;

    @Column(name = "landing_url", length = 200)
    private String landingUrl;
}
