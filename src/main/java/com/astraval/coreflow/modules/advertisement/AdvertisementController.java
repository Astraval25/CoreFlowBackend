package com.astraval.coreflow.modules.advertisement;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.advertisement.dto.AdvertisementPageDto;
import com.astraval.coreflow.modules.advertisement.dto.CreateAdvertisementDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    @PostMapping(value = "/admin/ads", consumes = { "multipart/form-data" })
    public ApiResponse<Map<String, Long>> createAdvertisement(
            @RequestParam("ad") String adJson,
            @RequestParam("file") MultipartFile file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CreateAdvertisementDto request = mapper.readValue(adJson, CreateAdvertisementDto.class);
            Long adId = advertisementService.createAdvertisement(request, file);
            return ApiResponseFactory.created(Map.of("adId", adId), "Advertisement created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        } catch (Exception e) {
            return ApiResponseFactory.error("Failed to process request: " + e.getMessage(), 400);
        }
    }

    @GetMapping("/ads")
    public ApiResponse<AdvertisementPageDto> getActiveAdvertisements(
            @RequestParam(required = false) String placement,
            @RequestParam(defaultValue = "0") int page) {
        try {
            AdvertisementPageDto ads = advertisementService.getActiveAdvertisements(placement, page);
            return ApiResponseFactory.accepted(ads, "Advertisements retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/admin/ads")
    public ApiResponse<AdvertisementPageDto> getAllAdvertisements(
            @RequestParam(defaultValue = "0") int page) {
        try {
            AdvertisementPageDto ads = advertisementService.getAllAdvertisements(page);
            return ApiResponseFactory.accepted(ads, "Advertisements retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/admin/ads/{adId}/activate")
    public ApiResponse<String> activateAdvertisement(@PathVariable Long adId) {
        try {
            advertisementService.activateAdvertisement(adId);
            return ApiResponseFactory.updated(null, "Advertisement activated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/admin/ads/{adId}/deactivate")
    public ApiResponse<String> deactivateAdvertisement(@PathVariable Long adId) {
        try {
            advertisementService.deactivateAdvertisement(adId);
            return ApiResponseFactory.updated(null, "Advertisement deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @DeleteMapping("/admin/ads/{adId}")
    public ApiResponse<String> deleteAdvertisement(@PathVariable Long adId) {
        try {
            advertisementService.deleteAdvertisement(adId);
            return ApiResponseFactory.deleted("Advertisement deleted successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
