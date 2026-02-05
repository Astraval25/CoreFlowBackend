package com.astraval.coreflow.modules.items;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.items.dto.CreateCustomerItemDto;
import com.astraval.coreflow.modules.items.dto.CustomerItemDetailDto;
import com.astraval.coreflow.modules.items.dto.CustomerItemSummaryDto;
import com.astraval.coreflow.modules.items.dto.UpdateCustomerItemDto;
import com.astraval.coreflow.modules.items.model.ItemCustomerPrice;
import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.items.repo.ItemCustomerPriceRepository;
import com.astraval.coreflow.modules.items.repo.ItemRepository;

@Service
public class CustomerItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemCustomerPriceRepository itemCustomerPriceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Long createCustomerItem(Long companyId, Long customerId, CreateCustomerItemDto request) {
        Customers customer = getCustomerOrThrow(companyId, customerId);
        Items item = getItemOrThrow(companyId, request.getItemId());

        ItemCustomerPrice existing = itemCustomerPriceRepository
                .findByItemItemIdAndCustomerCustomerId(item.getItemId(), customerId)
                .orElse(null);

        if (existing != null) {
            if (Boolean.TRUE.equals(existing.getIsActive())) {
                throw new RuntimeException("Customer item price already exists for item ID: " + item.getItemId());
            }
            existing.setSalesPrice(request.getSalesPrice());
            existing.setSalesDescription(request.getSalesDescription());
            existing.setIsActive(true);
            return itemCustomerPriceRepository.save(existing).getItemCustomerPriceId();
        }

        ItemCustomerPrice price = new ItemCustomerPrice();
        price.setItem(item);
        price.setCustomer(customer);
        price.setSalesPrice(request.getSalesPrice());
        price.setSalesDescription(request.getSalesDescription());
        return itemCustomerPriceRepository.save(price).getItemCustomerPriceId();
    }

    @Transactional
    public void updateCustomerItem(Long companyId, Long customerId, Long itemId, UpdateCustomerItemDto request) {
        getCustomerOrThrow(companyId, customerId);
        getItemOrThrow(companyId, itemId);

        if (request.getSalesPrice() == null && request.getSalesDescription() == null) {
            throw new RuntimeException("At least one of sales price or sales description is required");
        }

        ItemCustomerPrice price = itemCustomerPriceRepository
                .findByItemItemIdAndCustomerCustomerId(itemId, customerId)
                .orElseThrow(() -> new RuntimeException("Customer item price not found for item ID: " + itemId));

        if (request.getSalesPrice() != null) {
            price.setSalesPrice(request.getSalesPrice());
        }
        if (request.getSalesDescription() != null) {
            price.setSalesDescription(request.getSalesDescription());
        }

        itemCustomerPriceRepository.save(price);
    }

    @Transactional
    public void deactivateCustomerItem(Long companyId, Long customerId, Long itemId) {
        getCustomerOrThrow(companyId, customerId);
        getItemOrThrow(companyId, itemId);

        ItemCustomerPrice price = itemCustomerPriceRepository
                .findByItemItemIdAndCustomerCustomerId(itemId, customerId)
                .orElseThrow(() -> new RuntimeException("Customer item price not found for item ID: " + itemId));

        price.setIsActive(false);
        itemCustomerPriceRepository.save(price);
    }

    @Transactional
    public void activateCustomerItem(Long companyId, Long customerId, Long itemId) {
        getCustomerOrThrow(companyId, customerId);
        getItemOrThrow(companyId, itemId);

        ItemCustomerPrice price = itemCustomerPriceRepository
                .findByItemItemIdAndCustomerCustomerId(itemId, customerId)
                .orElseThrow(() -> new RuntimeException("Customer item price not found for item ID: " + itemId));

        price.setIsActive(true);
        itemCustomerPriceRepository.save(price);
    }

    public List<CustomerItemSummaryDto> getItemsByCustomer(Long companyId, Long customerId) {
        validateCustomer(companyId, customerId);
        List<Items> items = itemRepository.findByCompanyCompanyIdOrderByItemName(companyId);
        Map<Long, ItemCustomerPrice> priceByItemId = loadCustomerPriceMap(customerId);
        return items.stream()
                .map(item -> toSummaryDto(item, priceByItemId.get(item.getItemId())))
                .toList();
    }

    public List<CustomerItemSummaryDto> getActiveItemsByCustomer(Long companyId, Long customerId) {
        validateCustomer(companyId, customerId);
        List<Items> items = itemRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByItemName(companyId);
        Map<Long, ItemCustomerPrice> priceByItemId = loadCustomerPriceMap(customerId);
        return items.stream()
                .map(item -> toSummaryDto(item, priceByItemId.get(item.getItemId())))
                .toList();
    }

    public List<CustomerItemSummaryDto> getMappedItemsByCustomer(Long companyId, Long customerId) {
        validateCustomer(companyId, customerId);
        List<ItemCustomerPrice> prices = itemCustomerPriceRepository
                .findByCustomerCustomerIdAndIsActiveTrue(customerId);
        return prices.stream()
                .map(price -> toSummaryDto(price.getItem(), price))
                .toList();
    }

    public CustomerItemDetailDto getItemDetail(Long companyId, Long customerId, Long itemId) {
        validateCustomer(companyId, customerId);
        Items item = itemRepository.findByItemIdAndCompanyCompanyId(itemId, companyId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        ItemCustomerPrice price = itemCustomerPriceRepository
                .findByItemItemIdAndCustomerCustomerIdAndIsActiveTrue(itemId, customerId)
                .orElse(null);

        CustomerItemDetailDto dto = new CustomerItemDetailDto();
        dto.setItemId(item.getItemId());
        dto.setItemName(item.getItemName());
        dto.setItemType(item.getItemType());
        dto.setUnit(item.getUnit());
        dto.setSalesPrice(resolveSalesPrice(item, price));
        dto.setSalesDescription(resolveSalesDescription(item, price));
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

    private void validateCustomer(Long companyId, Long customerId) {
        customerRepository.findByCustomerIdAndCompanyCompanyId(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
    }

    private Customers getCustomerOrThrow(Long companyId, Long customerId) {
        return customerRepository.findByCustomerIdAndCompanyCompanyId(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
    }

    private Items getItemOrThrow(Long companyId, Long itemId) {
        return itemRepository.findByItemIdAndCompanyCompanyId(itemId, companyId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));
    }

    private Map<Long, ItemCustomerPrice> loadCustomerPriceMap(Long customerId) {
        List<ItemCustomerPrice> prices = itemCustomerPriceRepository
                .findByCustomerCustomerIdAndIsActiveTrue(customerId);
        return prices.stream()
                .collect(Collectors.toMap(p -> p.getItem().getItemId(), p -> p, (a, b) -> a));
    }

    private CustomerItemSummaryDto toSummaryDto(Items item, ItemCustomerPrice price) {
        return new CustomerItemSummaryDto(
                item.getItemId(),
                item.getItemName(),
                item.getItemType(),
                item.getUnit(),
                resolveSalesPrice(item, price),
                resolveSalesDescription(item, price),
                item.getHsnCode(),
                item.getTaxRate(),
                item.getIsActive());
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
}
