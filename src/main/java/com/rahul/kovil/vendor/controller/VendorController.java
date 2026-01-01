package com.rahul.kovil.vendor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.kovil.common.dto.QuickVendorDto;
import com.rahul.kovil.common.dto.VendorDto;
import com.rahul.kovil.common.enums.VendorType;
import com.rahul.kovil.config.JwtProvider;
import com.rahul.kovil.vendor.service.VendorService;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {

	private final JwtProvider jwt;
	
	private final VendorService vendorService;
	

	public VendorController(JwtProvider jwt, VendorService vendorService) {
		this.jwt = jwt;
		this.vendorService = vendorService;
	}
	
	@PostMapping("")
	public ResponseEntity<?> saveVendor(@RequestHeader("Authentication") String token, @RequestBody VendorDto vendor) {
		return ResponseEntity.ok(null);
	}
	
	@PostMapping("/quick-creation")
	public ResponseEntity<QuickVendorDto> quickVendorCreation(@RequestBody QuickVendorDto vendor){
		String tenantId = "";
		String userId = "";
		return ResponseEntity.status(HttpStatus.CREATED).body(vendorService.quickCreation(vendor, tenantId, userId));
	}
	
	@GetMapping("/search/{query}")
	public ResponseEntity<List<VendorDto>> serachVendor(@PathVariable String query) {
		return ResponseEntity.ok(vendorService.getVendorsByQuery(VendorType.DEVOTEE, query));
	}
	
}

