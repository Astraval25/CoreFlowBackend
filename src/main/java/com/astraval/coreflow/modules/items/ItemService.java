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
import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.customer.CustomerVendorLinkRepository;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.filestorage.FileStorage;
import com.astraval.coreflow.modules.filestorage.FileStorageRepository;
import com.astraval.coreflow.modules.filestorage.FileStorageService;
import com.astraval.coreflow.modules.items.dto.CreateItemDto;
import com.astraval.coreflow.modules.items.dto.ItemDetailDto;
import com.astraval.coreflow.modules.items.dto.ItemSummaryDto;
import com.astraval.coreflow.modules.items.dto.PurchasableItemDto;
import com.astraval.coreflow.modules.items.dto.SellableItemDto;
import com.astraval.coreflow.modules.items.dto.UpdateItemDto;
import com.astraval.coreflow.modules.items.model.ItemCustomerPrice;
import com.astraval.coreflow.modules.items.model.ItemStocks;
import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.items.model.ItemVendorPrice;
import com.astraval.coreflow.modules.items.repo.ItemCustomerPriceRepository;
import com.astraval.coreflow.modules.items.repo.ItemRepository;
import com.astraval.coreflow.modules.items.repo.ItemStocksRepository;
import com.astraval.coreflow.modules.items.repo.ItemVendorPriceRepository;
import com.astraval.coreflow.modules.vendor.VendorRepository;
import com.astraval.coreflow.modules.vendor.Vendors;

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

    @Autowired
    private CustomerVendorLinkRepository customerVendorLinkRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VendorRepository vendorRepository;

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

    public List<SellableItemDto> getSellableItemsByCompanyAndCustomer(
            Long companyId, Long customerId, Long customerCompanyId) {
        Customers customer = customerRepository.findByCustomerIdAndCompanyCompanyId(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        boolean isLinked = customerVendorLinkRepository
                .findByCustomerCustomerIdAndVendorCompanyId(customerId, customerCompanyId)
                .isPresent();

        if (!isLinked) {
            return itemRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByItemName(companyId).stream()
                    .filter(item -> item.getBaseSalesPrice() != null)
                    .map(item -> new SellableItemDto(
                            item.getItemId(),
                            item.getItemName(),
                            item.getSalesDescription(),
                            item.getBaseSalesPrice(),
                            item.getTaxRate(),
                            item.getHsnCode()))
                    .toList();
        }

        List<ItemCustomerPrice> prices = itemCustomerPriceRepository
                .findByCustomerCustomerIdAndIsActiveTrue(customerId);

        return prices.stream()
                .map(price -> new SellableItemDto(
                        price.getItem().getItemId(),
                        price.getItem().getItemName(),
                        resolveSalesDescription(price.getItem(), price),
                        resolveSalesPrice(price.getItem(), price),
                        price.getItem().getTaxRate(),
                        price.getItem().getHsnCode()))
                .toList();
    }

    public List<PurchasableItemDto> getPurchasableItemsByCompanyAndVendor(
            Long companyId, Long vendorId, Long vendorCompanyId) {
        Vendors vendor = vendorRepository.findByVendorIdAndCompanyCompanyId(vendorId, companyId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

        boolean isLinked = customerVendorLinkRepository
                .findByVendorVendorIdAndCustomerCompanyId(vendorId, vendorCompanyId)
                .isPresent();

        if (!isLinked) {
            return itemRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByItemName(companyId).stream()
                    .filter(item -> item.getBasePurchasePrice() != null)
                    .map(item -> new PurchasableItemDto(
                            item.getItemId(),
                            item.getItemName(),
                            item.getPurchaseDescription(),
                            item.getBasePurchasePrice(),
                            item.getTaxRate(),
                            item.getHsnCode(),
                            "ITEM_BASE"))
                    .toList();
        }

        List<ItemVendorPrice> prices = itemVendorPriceRepository
                .findByVendorVendorIdAndIsActiveTrue(vendorId);

        return prices.stream()
                .map(price -> new PurchasableItemDto(
                        price.getItem().getItemId(),
                        price.getItem().getItemName(),
                        resolvePurchaseDescription(price.getItem(), price),
                        resolvePurchasePrice(price.getItem(), price),
                        price.getItem().getTaxRate(),
                        price.getItem().getHsnCode(),
                        "VENDOR_ITEM"))
                .toList();
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

    private BigDecimal resolveSalesPrice(Items item, ItemCustomerPrice price) {
        if (price != null && price.getSalesPrice() != null) {
            return price.getSalesPrice();
        }
        return item.getBaseSalesPrice();
    }

    private String resolveSalesDescription(Items item, ItemCustomerPrice price) {
        if (price != null && price.getSalesDescription() != null) {
            return price.getSalesDescription();
        }
        return item.getSalesDescription();
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
