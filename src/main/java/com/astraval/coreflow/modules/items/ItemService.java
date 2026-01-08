package com.astraval.coreflow.modules.items;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.items.dto.CreateUpdateItemDto;
import com.astraval.coreflow.modules.items.dto.ItemSummaryDto;
import com.astraval.coreflow.modules.items.dto.UpdateItemDto;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ItemMapper itemMapper;


    @Transactional
    public Long createItem(Long companyId, CreateUpdateItemDto request) {
        try {
            // Validate that either salesPrice or purchasePrice is provided
            if (request.getSalesPrice() == null && request.getPurchasePrice() == null) {
                throw new RuntimeException("Either sales price or purchase price is required");
            }

            Companies company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

            Items item = new Items();
            item.setCompany(company);
            itemMapper.mapDtoToEntity(request, item);

            return itemRepository.save(item).getItemId();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create item: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updateItem(Long companyId, Long itemId, UpdateItemDto request) {
        try {
            Items item = itemRepository.findByItemIdAndCompanyCompanyId(itemId, companyId)
                    .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

            itemMapper.mapUpdateDtoToEntity(request, item);

            itemRepository.save(item);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update item: " + e.getMessage(), e);
        }
    }

    public List<Items> getAllItems() {
        return itemRepository.findAll();
    }

    public List<ItemSummaryDto> getItemsByCompany(Long companyId) {
        return itemRepository.findByCompanyIdSummary(companyId);
    }

    public List<ItemSummaryDto> getActiveItemsByCompany(Long companyId) {
        return itemRepository.findActiveByCompanyIdSummary(companyId);
    }

    public Items getItemById(Long companyId, Long itemId) {
        return itemRepository.findByItemIdAndCompanyCompanyId(itemId, companyId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));
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
}