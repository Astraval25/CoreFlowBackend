package com.astraval.coreflow.employee_module.workdef;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.employee_module.workdef.dto.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies/{companyId}/modemp/work-definitions")
public class WorkDefinitionController {

    @Autowired
    private WorkDefinitionService workDefinitionService;

    @PostMapping
    public ApiResponse<Map<String, Long>> createWorkDefinition(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateWorkDefinitionDto dto) {
        try {
            Long id = workDefinitionService.createWorkDefinition(companyId, dto);
            return ApiResponseFactory.created(Map.of("workDefId", id), "Work definition created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping
    public ApiResponse<List<WorkDefinitionDto>> getWorkDefinitions(
            @PathVariable Long companyId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        try {
            List<WorkDefinitionDto> list = activeOnly
                    ? workDefinitionService.getActiveWorkDefinitions(companyId)
                    : workDefinitionService.getWorkDefinitions(companyId);
            return ApiResponseFactory.accepted(list, "Work definitions retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{workDefId}")
    public ApiResponse<WorkDefinitionDto> getWorkDefinition(
            @PathVariable Long companyId,
            @PathVariable Long workDefId) {
        try {
            WorkDefinitionDto dto = workDefinitionService.getWorkDefinition(companyId, workDefId);
            return ApiResponseFactory.accepted(dto, "Work definition retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{workDefId}")
    public ApiResponse<Void> updateWorkDefinition(
            @PathVariable Long companyId,
            @PathVariable Long workDefId,
            @Valid @RequestBody UpdateWorkDefinitionDto dto) {
        try {
            workDefinitionService.updateWorkDefinition(companyId, workDefId, dto);
            return ApiResponseFactory.updated(null, "Work definition updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{workDefId}/deactivate")
    public ApiResponse<Void> deactivateWorkDefinition(
            @PathVariable Long companyId,
            @PathVariable Long workDefId) {
        try {
            workDefinitionService.deactivateWorkDefinition(companyId, workDefId);
            return ApiResponseFactory.updated(null, "Work definition deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{workDefId}/rate-history")
    public ApiResponse<List<RateHistoryDto>> getRateHistory(
            @PathVariable Long companyId,
            @PathVariable Long workDefId) {
        try {
            List<RateHistoryDto> list = workDefinitionService.getRateHistory(companyId, workDefId);
            return ApiResponseFactory.accepted(list, "Rate history retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
