package com.astraval.coreflow.modules.modemp.workdef;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.modemp.workdef.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkDefinitionService {

    @Autowired
    private WorkDefinitionRepository workDefinitionRepository;

    @Autowired
    private WorkDefinitionRateHistoryRepository rateHistoryRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Transactional
    public Long createWorkDefinition(Long companyId, CreateWorkDefinitionDto dto) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        if (workDefinitionRepository.existsByCompanyCompanyIdAndWorkCode(companyId, dto.getWorkCode())) {
            throw new RuntimeException("Work code '" + dto.getWorkCode() + "' already exists in this company");
        }

        WorkDefinition wd = new WorkDefinition();
        wd.setCompany(company);
        wd.setWorkName(dto.getWorkName());
        wd.setWorkCode(dto.getWorkCode());
        wd.setDescription(dto.getDescription());
        wd.setRatePerUnit(dto.getRatePerUnit());
        wd.setUnit(dto.getUnit());
        wd.setIsActive(true);

        WorkDefinition saved = workDefinitionRepository.save(wd);
        return saved.getWorkDefId();
    }

    public List<WorkDefinitionDto> getWorkDefinitions(Long companyId) {
        return workDefinitionRepository.findByCompanyCompanyIdOrderByWorkName(companyId)
                .stream().map(this::toDto).toList();
    }

    public List<WorkDefinitionDto> getActiveWorkDefinitions(Long companyId) {
        return workDefinitionRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByWorkName(companyId)
                .stream().map(this::toDto).toList();
    }

    public WorkDefinitionDto getWorkDefinition(Long companyId, Long workDefId) {
        WorkDefinition wd = workDefinitionRepository.findByWorkDefIdAndCompanyCompanyId(workDefId, companyId)
                .orElseThrow(() -> new RuntimeException("Work definition not found with ID: " + workDefId));
        return toDto(wd);
    }

    @Transactional
    public void updateWorkDefinition(Long companyId, Long workDefId, UpdateWorkDefinitionDto dto) {
        WorkDefinition wd = workDefinitionRepository.findByWorkDefIdAndCompanyCompanyId(workDefId, companyId)
                .orElseThrow(() -> new RuntimeException("Work definition not found with ID: " + workDefId));

        // If rate is changing, archive the old rate
        if (dto.getRatePerUnit() != null && dto.getRatePerUnit().compareTo(wd.getRatePerUnit()) != 0) {
            WorkDefinitionRateHistory history = new WorkDefinitionRateHistory();
            history.setWorkDefinition(wd);
            history.setRatePerUnit(wd.getRatePerUnit());
            history.setUnit(wd.getUnit());
            history.setEffectiveFrom(wd.getCreatedDt() != null ? wd.getCreatedDt().toLocalDate() : LocalDate.now());
            history.setEffectiveTo(LocalDate.now());
            history.setChangedBy(getCurrentUserId());
            history.setChangedDt(LocalDateTime.now());
            rateHistoryRepository.save(history);

            wd.setRatePerUnit(dto.getRatePerUnit());
        }

        if (dto.getWorkName() != null) wd.setWorkName(dto.getWorkName());
        if (dto.getDescription() != null) wd.setDescription(dto.getDescription());
        if (dto.getUnit() != null) wd.setUnit(dto.getUnit());

        workDefinitionRepository.save(wd);
    }

    @Transactional
    public void deactivateWorkDefinition(Long companyId, Long workDefId) {
        WorkDefinition wd = workDefinitionRepository.findByWorkDefIdAndCompanyCompanyId(workDefId, companyId)
                .orElseThrow(() -> new RuntimeException("Work definition not found with ID: " + workDefId));
        wd.setIsActive(false);
        workDefinitionRepository.save(wd);
    }

    public List<RateHistoryDto> getRateHistory(Long companyId, Long workDefId) {
        workDefinitionRepository.findByWorkDefIdAndCompanyCompanyId(workDefId, companyId)
                .orElseThrow(() -> new RuntimeException("Work definition not found with ID: " + workDefId));

        return rateHistoryRepository.findByWorkDefinitionWorkDefIdOrderByEffectiveFromDesc(workDefId)
                .stream()
                .map(h -> new RateHistoryDto(h.getRateHistoryId(), h.getRatePerUnit(),
                        h.getUnit(), h.getEffectiveFrom(), h.getEffectiveTo()))
                .toList();
    }

    private WorkDefinitionDto toDto(WorkDefinition wd) {
        return new WorkDefinitionDto(
                wd.getWorkDefId(), wd.getWorkName(), wd.getWorkCode(),
                wd.getDescription(), wd.getRatePerUnit(), wd.getUnit(), wd.getIsActive());
    }

    private Long getCurrentUserId() {
        try {
            return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return 0L;
        }
    }
}
