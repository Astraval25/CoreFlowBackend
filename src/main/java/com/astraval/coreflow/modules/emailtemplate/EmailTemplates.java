package com.astraval.coreflow.modules.emailtemplate;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_templates")
public class EmailTemplates {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "email_template_id")
  private Long emailTemplateId;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "from_email", length = 255)
  private String fromEmail;

  @Column(name = "to_email", length = 255)
  private String toEmail;

  @Column(name = "subject", nullable = false, length = 255)
  private String subject;

  @Column(name = "body_html", nullable = false, columnDefinition = "TEXT")
  private String bodyHtml;

  @Column(name = "body_text", nullable = false, columnDefinition = "TEXT")
  private String bodyText;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "type", nullable = false, length = 50)
  private String type;

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

}
