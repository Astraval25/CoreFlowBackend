package com.astraval.coreflow.modules.emailtemplate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailTemplateDetails {
  private String fromEmail;
  private String toEmail;
  private String subject;
  private String bodyHtml;
  private String bodyText;
  private String description;
  
}
