package com.rahul.kovil.inventory.controller;

import java.time.LocalDateTime;
import java.util.List;

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

import com.rahul.kovil.common.dto.InvItemDto;
import com.rahul.kovil.common.response.ApiResponse;
import com.rahul.kovil.config.JwtProvider;
import com.rahul.kovil.inventory.service.ItemService;

@RestController
@RequestMapping("/api/inv/item")
public class ItemController {

	@Autowired
	private JwtProvider jwt;
	
	private final ItemService itemService;
	
	public ItemController(ItemService itemService) {
		this.itemService = itemService;
	}
	
	@GetMapping("")
	public ResponseEntity<List<InvItemDto>> getAllItems(@RequestHeader("Authorization") String token){
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.status(HttpStatus.OK).body(itemService.getAllItems(tenantId));
	}
	
	@PostMapping("")
	public ResponseEntity<InvItemDto> addItem(@RequestHeader("Authorization") String token ,@RequestBody InvItemDto item){
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.status(HttpStatus.CREATED).body(itemService.saveItem(item, tenantId));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> deleteItem(@RequestHeader("Authorization") String token ,@PathVariable Long id){
		String tenantId = jwt.getTenantId(token);
		itemService.softDeleteItem(id);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder().message("Item deleted").status(HttpStatus.OK).timestamp(LocalDateTime.now()).build());
	}
	
	@GetMapping("/validate-code/{code}")
	public ResponseEntity<ApiResponse> getMethodName(@RequestHeader("Authorization") String token, @PathVariable String code) {
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.ok(itemService.isItemCodeAvailable(tenantId, code));
	}
	
	
	
	
	
	
	
}
