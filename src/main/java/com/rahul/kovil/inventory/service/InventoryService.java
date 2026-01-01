package com.rahul.kovil.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rahul.kovil.common.enums.ItemUnitType;
import com.rahul.kovil.common.enums.StockDirection;
import com.rahul.kovil.inventory.entity.Item;
import com.rahul.kovil.inventory.entity.StockTransaction;
import com.rahul.kovil.inventory.entity.UnitConversion;
import com.rahul.kovil.inventory.repository.ItemRepository;
import com.rahul.kovil.inventory.repository.StockTransactionRepository;
import com.rahul.kovil.inventory.repository.UnitConversionRepository;

@Service
public class InventoryService {
	@Autowired
	private ItemRepository itemRepo;

	@Autowired
	private StockTransactionRepository transactionRepo;

	@Autowired
	private UnitConversionRepository conversionRepo;

//	public StockTransaction addStock(Long itemId, StockDirection type, double quantity, ItemUnitType unit) {
//
//		double multiplier = getMultiplier(unit, item.getBaseUnit());
//		StockTransaction tx = new StockTransaction();
//		tx.setItem(itemId);
//		tx.setStockDirection(type);
//		tx.setQuantity(quantity);
//		tx.setTransactionUnit(unit);
//		tx.setUnitMultiplier(multiplier);
//		return transactionRepo.save(tx);
//	}
//
//	private double getMultiplier(ItemUnitType from, ItemUnitType to) {
//		if (from == to)
//			return 1.0;
//		UnitConversion conv = conversionRepo.findByFromUnitAndToUnit(from, to)
//				.orElseThrow(() -> new RuntimeException("Conversion not found"));
//		return conv.getMultiplier();
//	}
}
