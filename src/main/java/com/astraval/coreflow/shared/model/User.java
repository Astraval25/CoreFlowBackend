package com.astraval.coreflow.shared.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data

public class User {

    @Id
    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "first_name", length = 200)
    private String firstName;

    @Column(name = "last_name", length = 200)
    private String lastName;

    @Column(name = "user_name", length = 100, nullable = false, unique = true)
    private String userName;

    @Column(name = "pwd", length = 300, nullable = false)
    private String password;

    @Column(name = "contact_no", length = 10, nullable = false)
    private String contactNo;

    @Column(name = "email" ,length = 100, nullable = false)
    private String email;

    @OneToOne
    @JoinColumn(name = "default_company_id", nullable = false)
    private Companies defaultCompany;
    

    // default fields...
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by", length = 100, nullable = true)
    private String createdBy;

    @Column(name = "created_dt", nullable = true)
    private LocalDateTime createdDt;

    @Column(name = "modified_by", length = 100, nullable = true)
    private String modifiedBy;

    @Column(name = "modified_dt", nullable = true)
    private LocalDateTime modifiedDt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserCompanyMap> companyMapping;
}
