package com.astraval.coreflow.modules.advertisement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.astraval.coreflow.modules.advertisement.dto.AdvertisementPageDto;
import com.astraval.coreflow.modules.advertisement.dto.AdvertisementViewDto;
import com.astraval.coreflow.modules.advertisement.dto.CreateAdvertisementDto;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.filestorage.FileStorage;
import com.astraval.coreflow.modules.filestorage.FileStorageRepository;
import com.astraval.coreflow.modules.filestorage.FileStorageService;
import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.items.repo.ItemRepository;

@Service
public class AdvertisementService {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileStorageRepository fileStorageRepository;

    @Transactional
    public Long createAdvertisement(CreateAdvertisementDto request, MultipartFile file) {
        try {
            String normalizedPlacement = AdPlacement.normalizePlacement(request.getPlacement());
            if (normalizedPlacement == null) {
                throw new RuntimeException("Unsupported placement. Supported values: DASHBOARD_ADS, ORDER_PAGE_ADS");
            }

            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Ad image file is required");
            }

            Companies company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + request.getCompanyId()));

            Items item = null;
            if (request.getItemId() != null) {
                item = itemRepository.findById(request.getItemId())
                        .orElseThrow(() -> new RuntimeException("Item not found with ID: " + request.getItemId()));

                if (!item.getCompany().getCompanyId().equals(request.getCompanyId())) {
                    throw new RuntimeException("Item does not belong to the selected company");
                }
            }

            FileStorage fileStorage = fileStorageService.saveFile(file, "AD", request.getCompanyId().toString());
            FileStorage savedFile = fileStorageRepository.save(fileStorage);

            Advertisement advertisement = new Advertisement();
            advertisement.setCompany(company);
            advertisement.setItem(item);
            advertisement.setPlacement(normalizedPlacement);
            advertisement.setDescription(request.getDescription());
            advertisement.setActionUrl(request.getActionUrl());
            advertisement.setFsId(savedFile.getFsId());
            advertisement.setIsActive(true);

            return advertisementRepository.save(advertisement).getAdId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create advertisement: " + e.getMessage(), e);
        }
    }

    public AdvertisementPageDto getActiveAdvertisements(String placement, int page) {
        int pageSize = 6;
        int safePage = Math.max(page, 0);
        PageRequest pageRequest = PageRequest.of(safePage, pageSize, Sort.by(Sort.Direction.DESC, "createdDt"));

        Page<Advertisement> adPage;
        if (placement == null || placement.isBlank()) {
            adPage = advertisementRepository.findByIsActiveTrueOrderByCreatedDtDesc(pageRequest);
        } else {
            String normalizedPlacement = AdPlacement.normalizePlacement(placement);
            if (normalizedPlacement == null) {
                throw new RuntimeException("Unsupported placement. Supported values: DASHBOARD_ADS, ORDER_PAGE_ADS");
            }
            adPage = advertisementRepository.findByPlacementAndIsActiveTrueOrderByCreatedDtDesc(normalizedPlacement, pageRequest);
        }

        return toPageDto(adPage);
    }

    public AdvertisementPageDto getAllAdvertisements(int page) {
        int pageSize = 10;
        int safePage = Math.max(page, 0);
        PageRequest pageRequest = PageRequest.of(safePage, pageSize, Sort.by(Sort.Direction.DESC, "createdDt"));
        Page<Advertisement> adPage = advertisementRepository.findAllByOrderByCreatedDtDesc(pageRequest);
        return toPageDto(adPage);
    }

    @Transactional
    public void activateAdvertisement(Long adId) {
        Advertisement advertisement = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Advertisement not found with ID: " + adId));
        advertisement.setIsActive(true);
        advertisementRepository.save(advertisement);
    }

    @Transactional
    public void deactivateAdvertisement(Long adId) {
        Advertisement advertisement = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Advertisement not found with ID: " + adId));
        advertisement.setIsActive(false);
        advertisementRepository.save(advertisement);
    }

    @Transactional
    public void deleteAdvertisement(Long adId) {
        if (!advertisementRepository.existsById(adId)) {
            throw new RuntimeException("Advertisement not found with ID: " + adId);
        }
        advertisementRepository.deleteById(adId);
    }

    private AdvertisementPageDto toPageDto(Page<Advertisement> adPage) {
        AdvertisementPageDto response = new AdvertisementPageDto();
        response.setAdvertisements(adPage.getContent().stream().map(this::toViewDto).toList());
        response.setPage(adPage.getNumber());
        response.setSize(adPage.getSize());
        response.setTotalElements(adPage.getTotalElements());
        response.setTotalPages(adPage.getTotalPages());
        response.setHasNext(adPage.hasNext());
        response.setHasPrevious(adPage.hasPrevious());
        return response;
    }

    private AdvertisementViewDto toViewDto(Advertisement advertisement) {
        AdvertisementViewDto dto = new AdvertisementViewDto();
        dto.setAdId(advertisement.getAdId());
        dto.setCompanyId(advertisement.getCompany() != null ? advertisement.getCompany().getCompanyId() : null);
        dto.setCompanyName(advertisement.getCompany() != null ? advertisement.getCompany().getCompanyName() : null);
        dto.setItemId(advertisement.getItem() != null ? advertisement.getItem().getItemId() : null);
        dto.setItemName(advertisement.getItem() != null ? advertisement.getItem().getItemName() : null);
        dto.setPlacement(advertisement.getPlacement());
        dto.setDescription(advertisement.getDescription());
        dto.setActionUrl(advertisement.getActionUrl());
        dto.setFsId(advertisement.getFsId());
        dto.setIsActive(advertisement.getIsActive());
        dto.setCreatedDt(advertisement.getCreatedDt());
        return dto;
    }
}
