package com.astraval.coreflow.main_modules.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;

@Service
public class ConfigService {

    @Autowired
    private CompanyConfigRepository companyConfigRepo;

    @Autowired
    private ConfigDefinitionRepository configDefinitionRepo;

    @Autowired
    private CompanyRepository companyRepository;

    public String resolveConfig(Long companyId, String key) {
        return companyConfigRepo
            .findByCompanyCompanyIdAndConfigKey(companyId, key)
            .map(CompanyConfig::getConfigValue)
            .orElseGet(() -> configDefinitionRepo
                .findByConfigKey(key)
                .map(ConfigDefinition::getDefaultValue)
                .orElse(null));
    }

    public Map<String, String> resolveAllConfigs(Long companyId) {
        Map<String, String> result = configDefinitionRepo.findAll().stream()
            .collect(Collectors.toMap(
                ConfigDefinition::getConfigKey,
                ConfigDefinition::getDefaultValue,
                (a, b) -> a
            ));

        companyConfigRepo.findByCompanyCompanyId(companyId)
            .forEach(cc -> result.put(cc.getConfigKey(), cc.getConfigValue()));

        return result;
    }

    public List<ConfigDefinition> getAllDefinitions() {
        return configDefinitionRepo.findAll();
    }

    public void upsertCompanyConfig(Long companyId, String key, String value) {
        Companies company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found"));

        configDefinitionRepo.findByConfigKey(key)
            .orElseThrow(() -> new RuntimeException("Config key not found: " + key));

        CompanyConfig config = companyConfigRepo
            .findByCompanyCompanyIdAndConfigKey(companyId, key)
            .orElseGet(() -> {
                CompanyConfig c = new CompanyConfig();
                c.setCompany(company);
                c.setConfigKey(key);
                return c;
            });
        config.setConfigValue(value);
        companyConfigRepo.save(config);
    }

    public void resetToDefault(Long companyId, String key) {
        companyConfigRepo
            .findByCompanyCompanyIdAndConfigKey(companyId, key)
            .ifPresent(companyConfigRepo::delete);
    }
}
