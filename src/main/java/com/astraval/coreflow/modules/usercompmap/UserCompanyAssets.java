package com.astraval.coreflow.modules.usercompmap;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "v_user_company_assets")
@Data
public class UserCompanyAssets {
    
    @Id
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "company_id")
    private Long companyId;
    
    @Column(name = "customers")
    private Long[] customers;
    
    @Column(name = "vendors")
    private Long[] vendors;
    
    @Column(name = "items")
    private Long[] items;
}