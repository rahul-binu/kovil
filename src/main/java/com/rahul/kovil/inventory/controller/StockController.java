package com.rahul.kovil.inventory.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.kovil.common.dto.InvStockDto;
import com.rahul.kovil.common.response.ApiResponse;
import com.rahul.kovil.common.response.ToonResponse;
import com.rahul.kovil.config.JwtProvider;
import com.rahul.kovil.inventory.service.StockTransactionService;

@RestController
@RequestMapping("/api/inv/stock")
public class StockController {

	@Autowired
	private JwtProvider jwt;
	
	private final StockTransactionService stockTransactionService;
	
	public StockController(StockTransactionService stockTransactionService) {
		this.stockTransactionService = stockTransactionService;
	}	
	
	@PostMapping("")
	public ResponseEntity<InvStockDto> insertStock(@RequestHeader("Authorization") String token, @RequestBody InvStockDto stock){	
		String tenantId = jwt.getTenantId(token);
		InvStockDto savedStock = stockTransactionService.addOrUpdateStock(stock, tenantId);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedStock);
	}
	
	@GetMapping("/{date}")
	public ResponseEntity<Map<String, ToonResponse>> getCurrentStock(@RequestHeader("Authorization") String token, @PathVariable String date) {
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.ok(stockTransactionService.getCurrentStock(tenantId, date));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> deleteStockTransEntry(@PathVariable Long id){
		return ResponseEntity.ok(stockTransactionService.deleteStockTransEntry(id));
	}
	
	@GetMapping("/all-transactions")
	public ResponseEntity<Map<String, ToonResponse>> stockAllTransactions(@RequestHeader("Authorization") String token, @RequestParam String from, @RequestParam String to) {
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.ok(stockTransactionService.stockAllTransactions(tenantId, from, to));
	}
	
	
}
