package com.astraval.coreflow.modules.items;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.filestorage.FileStorage;
import com.astraval.coreflow.modules.filestorage.FileStorageRepository;
import com.astraval.coreflow.modules.filestorage.FileStorageService;
import com.astraval.coreflow.modules.items.dto.CreateItemDto;
import com.astraval.coreflow.modules.items.dto.GetOrderItemsDto;
import com.astraval.coreflow.modules.items.dto.ItemDetailDto;
import com.astraval.coreflow.modules.items.dto.ItemSummaryDto;
import com.astraval.coreflow.modules.items.dto.UpdateItemDto;
import com.astraval.coreflow.modules.items.model.ItemStocks;
import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.items.repo.ItemCustomerPriceRepository;
import com.astraval.coreflow.modules.items.repo.ItemRepository;
import com.astraval.coreflow.modules.items.dto.PurchasableItemDto;
import com.astraval.coreflow.modules.items.dto.SellableItemDto;
import com.astraval.coreflow.modules.items.repo.ItemVendorPriceRepository;
import com.astraval.coreflow.modules.items.repo.ItemStocksRepository;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileStorageRepository fileStorageRepository;

    @Autowired
    private ItemStocksRepository itemStocksRepository;

    @Autowired
    private ItemCustomerPriceRepository itemCustomerPriceRepository;

    @Autowired
    private ItemVendorPriceRepository itemVendorPriceRepository;


    @Transactional
    public Long createItem(Long companyId, CreateItemDto request, MultipartFile file) {
        try {
            if (request.getBaseSalesPrice() == null && request.getBasePurchasePrice() == null) {
                throw new RuntimeException("Either sales price or purchase price is required");
            }

            Companies company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

            Items item = new Items();
            item.setCompany(company);
            itemMapper.mapDtoToEntity(request, item);
            applySellablePurchasableFlags(item);

            // Handle file upload if provided
            if (file != null && !file.isEmpty()) {
                FileStorage fileStorage = fileStorageService.saveFile(file, "ITEM", companyId.toString());
                FileStorage savedFile = fileStorageRepository.save(fileStorage);
                item.setFsId(savedFile.getFsId());
            }

            Items savedItem = itemRepository.save(item);

            // Create initial stock entry
            ItemStocks itemStock = new ItemStocks();
            itemStock.setItem(savedItem);
            itemStock.setCompany(company);
            itemStock.setAvailableQty(BigDecimal.ZERO);
            itemStock.setReservedQty(BigDecimal.ZERO);
            itemStock.setLastUpdated(LocalDateTime.now());
            itemStocksRepository.save(itemStock);

            return savedItem.getItemId();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create item: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updateItem(Long companyId, Long itemId, UpdateItemDto request, MultipartFile file) {
        try {
            Items item = itemRepository.findByItemIdAndCompanyCompanyId(itemId, companyId)
                    .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

            itemMapper.mapUpdateDtoToEntity(request, item);
            applySellablePurchasableFlags(item);

            // Handle file upload if provided
            if (file != null && !file.isEmpty()) {
                FileStorage fileStorage = fileStorageService.saveFile(file, "ITEM", itemId.toString());
                FileStorage savedFile = fileStorageRepository.save(fileStorage);
                item.setFsId(savedFile.getFsId());
            }

            itemRepository.save(item);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update item: " + e.getMessage(), e);
        }
    }

    public List<Items> getAllItems() {
        return itemRepository.findAll();
    }

    public List<ItemSummaryDto> getItemsByCompany(Long companyId) {
        return itemRepository.findByCompanyCompanyIdOrderByItemName(companyId).stream()
                .map(this::mapToItemSummaryDto)
                .toList();
    }

    public List<ItemSummaryDto> getActiveItemsByCompany(Long companyId) {
        return itemRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByItemName(companyId).stream()
                .map(this::mapToItemSummaryDto)
                .toList();
    }

    public Items getItemById(Long companyId, Long itemId) {
        return itemRepository.findByItemIdAndCompanyCompanyId(itemId, companyId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));
    }

    public ItemDetailDto getItemDetail(Long companyId, Long itemId) {
        Items item = itemRepository.findByItemIdAndCompanyCompanyId(itemId, companyId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));
        
        ItemDetailDto dto = new ItemDetailDto();
        dto.setItemId(item.getItemId());
        dto.setItemName(item.getItemName());
        dto.setItemType(item.getItemType());
        dto.setUnit(item.getUnit());
        dto.setBaseSalesPrice(item.getBaseSalesPrice());
        dto.setSalesDescription(item.getSalesDescription());
        dto.setBasePurchasePrice(item.getBasePurchasePrice());
        dto.setPurchaseDescription(item.getPurchaseDescription());
        dto.setHsnCode(item.getHsnCode());
        dto.setTaxRate(item.getTaxRate());
        dto.setIsActive(item.getIsActive());
        dto.setIsSellable(item.getIsSellable());
        dto.setIsPurchasable(item.getIsPurchasable());
        dto.setCreatedBy(item.getCreatedBy());
        dto.setCreatedDt(item.getCreatedDt());
        dto.setLastModifiedBy(item.getLastModifiedBy());
        dto.setLastModifiedDt(item.getLastModifiedDt());
        dto.setItemImage(item.getFsId());
        
        return dto;
    }

    @Transactional
    public void deactivateItem(Long itemId) {
        Items item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));
        item.setIsActive(false);
        itemRepository.save(item);
    }

    @Transactional
    public void activateItem(Long itemId) {
        Items item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));
        item.setIsActive(true);
        itemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new RuntimeException("Item not found with ID: " + itemId);
        }
        itemRepository.deleteById(itemId);
    }
    
    public List<SellableItemDto> getSellableItemsByCompany(Long companyId) {
        return itemRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByItemName(companyId).stream()
                .filter(this::isSellable)
                .map(item -> new SellableItemDto(
                        item.getItemId(),
                        item.getItemName(),
                        item.getSalesDescription(),
                        item.getBaseSalesPrice(),
                        item.getTaxRate(),
                        item.getHsnCode()))
                .toList();
    }
    
    public List<PurchasableItemDto> getPurchasableItemsByCompany(Long companyId) {
        return itemRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByItemName(companyId).stream()
                .filter(this::isPurchasable)
                .map(item -> new PurchasableItemDto(
                        item.getItemId(),
                        item.getItemName(),
                        item.getPurchaseDescription(),
                        item.getBasePurchasePrice(),
                        item.getTaxRate(),
                        item.getHsnCode()))
                .toList();
    }
    
    public List<GetOrderItemsDto> getOrderItems(Long companyId, Long vendorId) {
        // Try Case 1 first: Linked vendor - get items from vendor's company for this customer
        List<Items> items = itemRepository.findItemsForLinkedVendor(companyId, vendorId);
        
        if (!items.isEmpty()) {
            return items.stream()
                    .map(item -> new GetOrderItemsDto(
                            item.getItemId(),
                            item.getItemName(),
                            item.getBaseSalesPrice() != null ? item.getBaseSalesPrice().doubleValue() : null,
                            item.getSalesDescription(),
                            item.getHsnCode(),
                            item.getTaxRate() != null ? item.getTaxRate().doubleValue() : null,
                            item.getUnit() != null ? item.getUnit().name() : null))
                    .toList();
        } else {
            // Case 2: Unlinked vendor - get items from company
            items = itemRepository.findItemsForUnlinkedVendor(companyId);
            return items.stream()
                    .map(item -> new GetOrderItemsDto(
                            item.getItemId(),
                            item.getItemName(),
                            item.getBasePurchasePrice() != null ? item.getBasePurchasePrice().doubleValue() : null,
                            item.getPurchaseDescription(),
                            item.getHsnCode(),
                            item.getTaxRate() != null ? item.getTaxRate().doubleValue() : null,
                            item.getUnit() != null ? item.getUnit().name() : null))
                    .toList();
        }
    }

    private boolean isSellable(Items item) {
        return Boolean.TRUE.equals(item.getIsSellable());
    }

    private boolean isPurchasable(Items item) {
        return Boolean.TRUE.equals(item.getIsPurchasable());
    }

    private ItemSummaryDto mapToItemSummaryDto(Items item) {
        return new ItemSummaryDto(
                item.getItemId(),
                item.getItemName(),
                item.getItemType(),
                item.getUnit(),
                item.getBaseSalesPrice(),
                item.getBasePurchasePrice(),
                item.getIsActive(),
                item.getIsSellable(),
                item.getIsPurchasable());
    }

    private void applySellablePurchasableFlags(Items item) {
        item.setIsSellable(item.getBaseSalesPrice() != null);
        item.setIsPurchasable(item.getBasePurchasePrice() != null);
    }
}
