package com.astraval.coreflow.modules.items;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.modules.items.dto.VendorItemDetailDto;
import com.astraval.coreflow.modules.items.dto.VendorItemSummaryDto;
import com.astraval.coreflow.modules.items.model.ItemVendorPrice;
import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.items.repo.ItemRepository;
import com.astraval.coreflow.modules.items.repo.ItemVendorPriceRepository;
import com.astraval.coreflow.modules.vendor.VendorRepository;

@Service
public class VendorItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemVendorPriceRepository itemVendorPriceRepository;

    @Autowired
    private VendorRepository vendorRepository;

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
        return dto;
    }

    private void validateVendor(Long companyId, Long vendorId) {
        vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));
    }

    private Map<Long, ItemVendorPrice> loadVendorPriceMap(Long vendorId) {
        List<ItemVendorPrice> prices = itemVendorPriceRepository
                .findByVendorVendorIdAndIsActiveTrue(vendorId);
        return prices.stream()
                .collect(Collectors.toMap(p -> p.getItem().getItemId(), p -> p, (a, b) -> a));
    }

    private VendorItemSummaryDto toSummaryDto(Items item, ItemVendorPrice price) {
        return new VendorItemSummaryDto(
                item.getItemId(),
                item.getItemName(),
                item.getItemType(),
                item.getUnit(),
                resolvePurchasePrice(item, price),
                resolvePurchaseDescription(item, price),
                item.getHsnCode(),
                item.getTaxRate(),
                item.getIsActive());
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
