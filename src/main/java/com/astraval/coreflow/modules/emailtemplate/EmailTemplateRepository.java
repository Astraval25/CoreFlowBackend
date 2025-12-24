package com.astraval.coreflow.modules.emailtemplate;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.astraval.coreflow.modules.emailtemplate.dto.EmailTemplateDetails;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplates, Long>{

    @Query("""
        SELECT new com.astraval.coreflow.modules.emailtemplate.dto.EmailTemplateDetails(
        et.fromEmail,
        et.toEmail,
        et.subject, 
        et.bodyHtml,
        et.bodyText,
        et.description
        )
        FROM EmailTemplates et
        WHERE et.name = :name AND et.isActive = true
        """)
    Optional<EmailTemplateDetails> findTemplateDetailByName(@Param("name") String name);
}
