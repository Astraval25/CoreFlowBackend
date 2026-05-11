package com.astraval.coreflow.main_modules.marketplace;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.items.repo.ItemRepository;
import com.astraval.coreflow.main_modules.marketplace.dto.MarketplaceCompanyDto;
import com.astraval.coreflow.main_modules.marketplace.dto.MarketplaceItemDto;

@Service
public class MarketplaceService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ItemRepository itemRepository;

    public List<MarketplaceCompanyDto> getMarketplaceCompanies() {
        return companyRepository.findMarketplaceCompanies()
                .stream()
                .map(this::toCompanyDto)
                .toList();
    }

    public MarketplaceCompanyDto getMarketplaceCompany(Long companyId) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        if (!Boolean.TRUE.equals(company.getIsActive())) {
            throw new RuntimeException("Company is inactive");
        }

        return toCompanyDto(company);
    }

    public List<MarketplaceItemDto> getMarketplaceCompanyItems(Long companyId) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        if (!Boolean.TRUE.equals(company.getIsActive())) {
            throw new RuntimeException("Company is inactive");
        }

        return itemRepository.findMarketplaceSellableItemsByCompanyId(companyId);
    }

    private MarketplaceCompanyDto toCompanyDto(Companies company) {
        return new MarketplaceCompanyDto(
                company.getCompanyId(),
                company.getCompanyName(),
                company.getIndustry(),
                company.getShortName(),
                company.getFsId(),
                company.getContactPerson(),
                company.getContactEmail(),
                company.getContactPhone(),
                company.getWebsite(),
                company.getAddressLine1(),
                company.getAddressLine2(),
                company.getCity(),
                company.getState(),
                company.getCountry(),
                company.getPostalCode(),
                company.getPublicDescription());
    }
}
