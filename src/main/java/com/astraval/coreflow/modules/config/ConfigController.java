package com.astraval.coreflow.modules.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;

@RestController
@RequestMapping("/api/companies/{companyId}/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getAllConfigs(
            @PathVariable Long companyId) {
        Map<String, String> configs = configService.resolveAllConfigs(companyId);
        return ResponseEntity.ok(ApiResponseFactory.ok(configs, "Company configs retrieved"));
    }

    @PutMapping("/{configKey}")
    public ResponseEntity<ApiResponse<String>> setConfig(
            @PathVariable Long companyId,
            @PathVariable String configKey,
            @RequestBody Map<String, String> body) {
        String value = body.get("configValue");
        if (value == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponseFactory.badRequest("configValue is required"));
        }
        configService.upsertCompanyConfig(companyId, configKey, value);
        return ResponseEntity.ok(ApiResponseFactory.ok("Config updated", "Config '" + configKey + "' updated"));
    }

    @DeleteMapping("/{configKey}")
    public ResponseEntity<ApiResponse<String>> resetConfig(
            @PathVariable Long companyId,
            @PathVariable String configKey) {
        configService.resetToDefault(companyId, configKey);
        return ResponseEntity.ok(ApiResponseFactory.ok("Config reset to default", "Config '" + configKey + "' reset to default"));
    }
}
