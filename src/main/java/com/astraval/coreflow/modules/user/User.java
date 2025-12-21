package com.astraval.coreflow.modules.user;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.usercompmap.UserCompanyMap;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "first_name", length = 200)
    private String firstName;

    @Column(name = "last_name", length = 200)
    private String lastName;

    @Column(name = "user_name", length = 100, nullable = false, unique = true)
    private String userName;

    @Column(name = "password", length = 300, nullable = false)
    private String password;

    @Column(name = "contact_no", length = 10)
    private String contactNo;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "verified")
    private boolean verified = false;

    @OneToOne
    @JoinColumn(name = "default_company_id", nullable = false)
    private Companies defaultCompany;
    

    // default fields...
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreatedBy
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @LastModifiedBy
    @Column(name = "modified_by")
    private Long lastModifiedBy;

    @LastModifiedDate
    @Column(name = "modified_dt")
    private LocalDateTime lastModifiedDt;
    
    
    // internal use only variable
    // @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserCompanyMap> companyMapping;
}
