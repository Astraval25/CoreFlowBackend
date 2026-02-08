package com.astraval.coreflow.modules.items.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.items.dto.CreateVendorItemDto;
import com.astraval.coreflow.modules.items.dto.VendorItemDetailDto;
import com.astraval.coreflow.modules.items.dto.VendorItemSummaryDto;
import com.astraval.coreflow.modules.items.dto.UpdateVendorItemDto;
import com.astraval.coreflow.modules.items.model.ItemVendorPrice;
import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.items.repo.ItemRepository;
import com.astraval.coreflow.modules.items.repo.ItemVendorPriceRepository;
import com.astraval.coreflow.modules.vendor.Vendors;
import com.astraval.coreflow.modules.vendor.VendorRepository;

@Service
public class VendorItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemVendorPriceRepository itemVendorPriceRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Transactional
    public Long createVendorItem(Long companyId, Long vendorId, CreateVendorItemDto request) {
        Vendors vendor = getVendorOrThrow(companyId, vendorId);
        Items item = getItemOrThrow(companyId, request.getItemId());

        ItemVendorPrice existing = itemVendorPriceRepository
                .findByItemItemIdAndVendorVendorId(item.getItemId(), vendorId)
                .orElse(null);

        if (existing != null) {
            if (Boolean.TRUE.equals(existing.getIsActive())) {
                throw new RuntimeException("Vendor item price already exists for item ID: " + item.getItemId());
            }
            existing.setPurchasePrice(request.getPurchasePrice());
            existing.setPurchaseDescription(request.getPurchaseDescription());
            existing.setIsActive(true);
            return itemVendorPriceRepository.save(existing).getItemVendorPriceId();
        }

        ItemVendorPrice price = new ItemVendorPrice();
        price.setItem(item);
        price.setVendor(vendor);
        price.setPurchasePrice(request.getPurchasePrice());
        price.setPurchaseDescription(request.getPurchaseDescription());
        return itemVendorPriceRepository.save(price).getItemVendorPriceId();
    }

    @Transactional
    public void updateVendorItem(Long companyId, Long vendorId, Long itemId, UpdateVendorItemDto request) {
        getVendorOrThrow(companyId, vendorId);
        getItemOrThrow(companyId, itemId);

        if (request.getPurchasePrice() == null && request.getPurchaseDescription() == null) {
            throw new RuntimeException("At least one of purchase price or purchase description is required");
        }

        ItemVendorPrice price = itemVendorPriceRepository
                .findByItemItemIdAndVendorVendorId(itemId, vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor item price not found for item ID: " + itemId));

        if (request.getPurchasePrice() != null) {
            price.setPurchasePrice(request.getPurchasePrice());
        }
        if (request.getPurchaseDescription() != null) {
            price.setPurchaseDescription(request.getPurchaseDescription());
        }

        itemVendorPriceRepository.save(price);
    }

    @Transactional
    public void deactivateVendorItem(Long companyId, Long vendorId, Long itemId) {
        getVendorOrThrow(companyId, vendorId);
        getItemOrThrow(companyId, itemId);

        ItemVendorPrice price = itemVendorPriceRepository
                .findByItemItemIdAndVendorVendorId(itemId, vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor item price not found for item ID: " + itemId));

        price.setIsActive(false);
        itemVendorPriceRepository.save(price);
    }

    @Transactional
    public void activateVendorItem(Long companyId, Long vendorId, Long itemId) {
        getVendorOrThrow(companyId, vendorId);
        getItemOrThrow(companyId, itemId);

        ItemVendorPrice price = itemVendorPriceRepository
                .findByItemItemIdAndVendorVendorId(itemId, vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor item price not found for item ID: " + itemId));

        price.setIsActive(true);
        itemVendorPriceRepository.save(price);
    }

    public List<VendorItemSummaryDto> getItemsByVendor(Long companyId, Long vendorId) {
        validateVendor(companyId, vendorId);
        List<Items> items = itemRepository.findByCompanyCompanyIdOrderByItemName(companyId);
        Map<Long, ItemVendorPrice> priceByItemId = loadVendorPriceMap(vendorId);
        return items.stream()
                .map(item -> toSummaryDto(item, priceByItemId.get(item.getItemId())))
                .toList();
    }

    public List<VendorItemSummaryDto> getActiveItemsByVendor(Long companyId, Long vendorId) {
        validateVendor(companyId, vendorId);
        List<Items> items = itemRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByItemName(companyId);
        Map<Long, ItemVendorPrice> priceByItemId = loadVendorPriceMap(vendorId);
        return items.stream()
                .map(item -> toSummaryDto(item, priceByItemId.get(item.getItemId())))
                .toList();
    }

    public List<VendorItemSummaryDto> getMappedItemsByVendor(Long companyId, Long vendorId) {
        validateVendor(companyId, vendorId);
        List<ItemVendorPrice> prices = itemVendorPriceRepository
                .findByVendorVendorIdAndIsActiveTrue(vendorId);
        return prices.stream()
                .map(price -> toSummaryDto(price.getItem(), price))
                .toList();
    }

    public VendorItemDetailDto getItemDetail(Long companyId, Long vendorId, Long itemId) {
        validateVendor(companyId, vendorId);
        Items item = itemRepository.findByItemIdAndCompanyCompanyId(itemId, companyId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        ItemVendorPrice price = itemVendorPriceRepository
                .findByItemItemIdAndVendorVendorIdAndIsActiveTrue(itemId, vendorId)
                .orElse(null);

        VendorItemDetailDto dto = new VendorItemDetailDto();
        dto.setItemId(item.getItemId());
        dto.setItemName(item.getItemName());
        dto.setItemType(item.getItemType());
        dto.setUnit(item.getUnit());
        dto.setPurchasePrice(resolvePurchasePrice(item, price));
        dto.setPurchaseDescription(resolvePurchaseDescription(item, price));
        dto.setHsnCode(item.getHsnCode());
        dto.setTaxRate(item.getTaxRate());
        dto.setIsActive(item.getIsActive());
        dto.setCreatedBy(item.getCreatedBy());
        dto.setCreatedDt(item.getCreatedDt());
        dto.setLastModifiedBy(item.getLastModifiedBy());
        dto.setLastModifiedDt(item.getLastModifiedDt());
        dto.setItemImage(item.getFsId());
        dto.setFsId(item.getFsId());
        return dto;
    }

    private void validateVendor(Long companyId, Long vendorId) {
        vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));
    }

    private Vendors getVendorOrThrow(Long companyId, Long vendorId) {
        return vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));
    }

    private Items getItemOrThrow(Long companyId, Long itemId) {
        return itemRepository.findByItemIdAndCompanyCompanyId(itemId, companyId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));
    }

    private Map<Long, ItemVendorPrice> loadVendorPriceMap(Long vendorId) {
        List<ItemVendorPrice> prices = itemVendorPriceRepository
                .findByVendorVendorIdAndIsActiveTrue(vendorId);
        return prices.stream()
                .collect(Collectors.toMap(p -> p.getItem().getItemId(), p -> p, (a, b) -> a));
    }

    private VendorItemSummaryDto toSummaryDto(Items item, ItemVendorPrice price) {
        String source = (price != null && (price.getPurchasePrice() != null || price.getPurchaseDescription() != null))
                ? "CUSTOMER_ITEM"
                : "ITEM_BASE";
        return new VendorItemSummaryDto(
                item.getItemId(),
                item.getItemName(),
                item.getItemType(),
                item.getUnit(),
                resolvePurchasePrice(item, price),
                resolvePurchaseDescription(item, price),
                item.getHsnCode(),
                item.getTaxRate(),
                item.getIsActive(),
                source,
                item.getFsId());
    }

    private BigDecimal resolvePurchasePrice(Items item, ItemVendorPrice price) {
        if (price != null && price.getPurchasePrice() != null) {
            return price.getPurchasePrice();
        }
        return item.getBasePurchasePrice();
    }

    private String resolvePurchaseDescription(Items item, ItemVendorPrice price) {
        if (price != null && price.getPurchaseDescription() != null) {
            return price.getPurchaseDescription();
        }
        return item.getPurchaseDescription();
    }
}
