package com.astraval.coreflow.main_modules.marketplace;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.main_modules.marketplace.dto.MarketplaceCompanyDto;
import com.astraval.coreflow.main_modules.marketplace.dto.MarketplaceItemDto;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    @Autowired
    private MarketplaceService marketplaceService;

    @GetMapping("/companies")
    public ApiResponse<List<MarketplaceCompanyDto>> getMarketplaceCompanies() {
        try {
            List<MarketplaceCompanyDto> data = marketplaceService.getMarketplaceCompanies();
            return ApiResponseFactory.accepted(data, "Marketplace companies retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/companies/{companyId}")
    public ApiResponse<MarketplaceCompanyDto> getMarketplaceCompany(@PathVariable Long companyId) {
        try {
            MarketplaceCompanyDto data = marketplaceService.getMarketplaceCompany(companyId);
            return ApiResponseFactory.accepted(data, "Marketplace company retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/companies/{companyId}/items")
    public ApiResponse<List<MarketplaceItemDto>> getMarketplaceCompanyItems(@PathVariable Long companyId) {
        try {
            List<MarketplaceItemDto> data = marketplaceService.getMarketplaceCompanyItems(companyId);
            return ApiResponseFactory.accepted(data, "Marketplace items retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
