package com.rahul.kovil.inventory.service;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.rahul.kovil.common.dto.InvConversionUnitDto;
import com.rahul.kovil.common.dto.InvItemDto;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.response.ApiResponse;
import com.rahul.kovil.config.TenantConstants;
import com.rahul.kovil.inventory.entity.Item;
import com.rahul.kovil.inventory.entity.UnitConversion;
import com.rahul.kovil.inventory.repository.ItemRepository;
import com.rahul.kovil.inventory.repository.UnitConversionRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ItemService {

	@Autowired
	private ModelMapper modelMapper;

	private final ItemRepository itemRepository;
	private final UnitConversionRepository unitConversionRepository;
	
	public ItemService(ItemRepository itemRepository, UnitConversionRepository unitConversionRepository) {
		this.itemRepository = itemRepository;
		this.unitConversionRepository = unitConversionRepository;
	}

	@Transactional
	public InvItemDto saveItem(final InvItemDto dto, final String tenantId) {

		final Item item;

		if (dto.getId() == null) {
			item = modelMapper.map(dto, Item.class);
		} else {
			item = itemRepository.findByIdAndStatus(dto.getId(), BaseStatus.ACTIVE)
					.orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + dto.getId()));

			modelMapper.map(dto, item);
		}
		item.setTenantId(tenantId);
		Item savedItem = itemRepository.save(item);
		return modelMapper.map(savedItem, InvItemDto.class);
	}

	public List<InvItemDto> getAllItems(String tenantId) {
		List<Item> items = itemRepository.findByTenantIdAndStatus(tenantId, BaseStatus.ACTIVE);
		List<InvItemDto> itemDtos = items.stream().map(i -> modelMapper.map(i, InvItemDto.class)).toList();
		return itemDtos;
	}

	@Transactional
	public void softDeleteItem(Long id) {
		Item item = itemRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
		item.setStatus(BaseStatus.CANCELED);
		itemRepository.save(item);
	}

	public ApiResponse isItemCodeAvailable(String tenantId, String itemCode) {

		List<String> tenantScope = List.of(tenantId, TenantConstants.GLOBAL_TENANT);

		boolean exists = itemRepository.existsByItemCodeAndTenantIdInAndStatus(itemCode, tenantScope,
				BaseStatus.ACTIVE);

		return ApiResponse.builder().status(HttpStatus.OK).message(exists + "").timestamp(LocalDateTime.now()).build();
	}

	public List<InvConversionUnitDto> getUnitConversions(String tenantId) {
		List<UnitConversion> units = unitConversionRepository.findByTenantIdIn(List.of(tenantId ,TenantConstants.GLOBAL_TENANT));
		return units.stream().map(u->modelMapper.map(u, InvConversionUnitDto.class)).toList();
	}

}
