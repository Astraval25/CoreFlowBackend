package com.astraval.coreflow.modules.address;

import lombok.Data;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "address")
@Data
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;

    @Column(name = "attention_name", length = 128)
    private String attentionName;

    @Column(name = "country", length = 128, nullable = false)
    private String country;

    @Column(name = "line1", length = 256, nullable = false)
    private String line1;

    @Column(name = "line2", length = 256)
    private String line2;

    @Column(name = "city", length = 128, nullable = false)
    private String city;

    @Column(name = "state", length = 128, nullable = false)
    private String state;

    @Column(name = "pincode", nullable = false)
    private Integer pincode;

    @Column(name = "phone", length = 128)
    private String phone;

    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_dt")
    private LocalDateTime modifiedDt;
}