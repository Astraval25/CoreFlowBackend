package com.astraval.coreflow.employee_module.portaluser;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.astraval.coreflow.employee_module.employee.Employee;
import com.astraval.coreflow.main_modules.companies.Companies;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_portal_users", schema = "modemp")
public class EmployeePortalUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portal_user_id")
    private Long portalUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @Column(name = "username", length = 100, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 300, nullable = false)
    private String password;

    @Column(name = "last_login_dt")
    private LocalDateTime lastLoginDt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @Column(name = "modified_dt")
    private LocalDateTime modifiedDt;

    @PrePersist
    protected void onCreate() {
        createdDt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDt = LocalDateTime.now();
    }
}
