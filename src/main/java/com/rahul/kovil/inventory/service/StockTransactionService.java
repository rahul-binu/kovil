package com.rahul.kovil.inventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.rahul.kovil.common.dto.InvStockDto;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.response.ApiResponse;
import com.rahul.kovil.common.response.ToonResponse;
import com.rahul.kovil.config.TenantConstants;
import com.rahul.kovil.inventory.entity.StockTransaction;
import com.rahul.kovil.inventory.repository.ItemRepository;
import com.rahul.kovil.inventory.repository.StockTransactionRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class StockTransactionService {

	@Autowired
	private ModelMapper modelMapper;

	private final ItemRepository itemRepository;
	private final StockTransactionRepository stockTransactionRepository;

	public StockTransactionService(StockTransactionRepository stockTransactionRepository,
			ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
		this.stockTransactionRepository = stockTransactionRepository;
	}

	@Transactional
	public InvStockDto addOrUpdateStock(InvStockDto dto, String tenantId) {

		StockTransaction stock;
		if (dto.getId() == null) {
			stock = modelMapper.map(dto, StockTransaction.class);
		} else {
			stock = stockTransactionRepository.findById(dto.getId())
					.orElseThrow(() -> new EntityNotFoundException("Stock not found with id: " + dto.getId()));
			modelMapper.map(dto, stock);
		}
		stock.setStatus(BaseStatus.ACTIVE);
		stock.setTenantId(tenantId);

		InvStockDto savedStock = modelMapper.map(stockTransactionRepository.save(stock), InvStockDto.class);
		return savedStock;
	}

	@Transactional
	public ApiResponse deleteStock(Long id) {

		StockTransaction stock = stockTransactionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Stock not found with id: " + id));
		stock.setStatus(BaseStatus.CANCELED);

		stockTransactionRepository.save(stock);

		return ApiResponse.builder().status(HttpStatus.OK).message("Stock entry deleted successfully")
				.timestamp(LocalDateTime.now()).build();
	}

	public Map<String, ToonResponse> getCurrentStock(String tenantId, String dateTime) {
		Map<String, ToonResponse> res = new HashMap<>();

		LocalDateTime till = LocalDateTime.parse(dateTime);

		List<Object[]> stockTransData = stockTransactionRepository.findCurrentStockByTransactionDateAndTenantId(till,
				List.of(tenantId, TenantConstants.GLOBAL_TENANT), List.of(BaseStatus.ACTIVE));
		List<String> stockTransLabels = List.of("item", "qty");
		ToonResponse stock = ToonResponse.builder().status("succes").message("Stock data till: " + till)
				.data(stockTransData).label(stockTransLabels).build();

		Set<Long> itemIds = stockTransData.stream().map(row -> (Long) row[0]).collect(Collectors.toSet());

		List<Object[]> itemData = itemRepository.findItemForStockByItemIdIn(itemIds);

		List<String> itemLabels = List.of("id", "itemCode", "itemName", "itemGroup", "baseUnit");
		ToonResponse item = ToonResponse.builder().status("success").message("Item data").data(itemData)
				.label(itemLabels).build();

		res.put("stock", stock);
		res.put("item", item);

		return res;
	}

	public Map<String, ToonResponse> stockAllTransactions(String tenantId, String from, String to) {

		Map<String, ToonResponse> res = new HashMap<>();

		LocalDateTime fromDate = LocalDate.parse(from).atStartOfDay();
		LocalDateTime toDate  = LocalDate.parse(to).atStartOfDay();

		List<Object[]> stockTransData = stockTransactionRepository.findStockTransactionBetweenTransactionDateAndTenantId(fromDate, toDate,
				List.of(tenantId, TenantConstants.GLOBAL_TENANT), List.of(BaseStatus.ACTIVE));
		List<String> stockTransLabels = List.of("item", "stkDir", "transTyp", "quantity", "transUnit", "unitMul", "remark", "transDt");
//		"s.item, s.stockDirection, s.transactionType, s.quantity, s.transactionUnit, s.unitMultiplier, s.remarks, s.transactionDate";
		ToonResponse stock = ToonResponse.builder().status("succes").message("Stock transaction")
				.data(stockTransData).label(stockTransLabels).build();

		res.put("stock", stock);

		return res;
	}

}
