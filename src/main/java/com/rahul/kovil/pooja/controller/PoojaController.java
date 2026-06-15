package com.rahul.kovil.pooja.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.kovil.common.dto.OfferingDto;
import com.rahul.kovil.common.dto.PoojaAdvanceCloseRequestDto;
import com.rahul.kovil.common.dto.PoojaMasterDto;
import com.rahul.kovil.common.dto.PoojaTransactonDto;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.response.ApiResponse;
import com.rahul.kovil.common.util.SiteHelper;
import com.rahul.kovil.config.JwtProvider;
import com.rahul.kovil.pooja.repository.PoojaTransactionRepository;
import com.rahul.kovil.pooja.service.PoojaMasterService;
import com.rahul.kovil.pooja.service.PoojaService;

import io.jsonwebtoken.Jwt;

@RestController
@RequestMapping("/api/pooja")
public class PoojaController {

	private final JwtProvider jwt;

	private final PoojaMasterService poojaMasterService;
	private final PoojaService poojaService;
	private final PoojaTransactionRepository poojaTransactionRepository;

	public PoojaController(JwtProvider jwt, PoojaMasterService poojaMaster, PoojaService poojaService,
			PoojaTransactionRepository poojaTransactionRepository) {
		this.jwt = jwt;
		this.poojaMasterService = poojaMaster;
		this.poojaService = poojaService;
		this.poojaTransactionRepository = poojaTransactionRepository;
	}

	@PostMapping("/master")
	public ResponseEntity<PoojaMasterDto> savePoojaMaster(@RequestBody PoojaMasterDto pooja) {
		String tenatId = "abcd";
		return ResponseEntity.ok(poojaMasterService.savePoojaMaster(pooja, tenatId));
	}

	@PutMapping("/master")
	public ResponseEntity<PoojaMasterDto> updatePoojaMaster(@RequestBody PoojaMasterDto pooja) {
		String tenatId = "abcd";
		return ResponseEntity.ok(poojaMasterService.updatePoojaMaster(pooja, tenatId));
	}

	@GetMapping("/master-data")
	public ResponseEntity<List<PoojaMasterDto>> poojaMasterData() {
		String tenantId = "abcd";
		return ResponseEntity.ok(poojaMasterService.getPoojaMasterData(tenantId, BaseStatus.ACTIVE));
	}

	@DeleteMapping("/master/{id}")
	public ResponseEntity<ApiResponse> deletePoojaMaster(@PathVariable Long id) {

		String tenantId = "abcd";

		poojaMasterService.softDelete(id, tenantId);

		ApiResponse res = ApiResponse.builder().message("Pooja Master deleted successfully").status(HttpStatus.OK)
				.build();

		return ResponseEntity.ok(res);
	}

	@GetMapping("/nakshathra-data")
	public ResponseEntity<List<String>> nakshathraData() {
		return ResponseEntity.ok(poojaMasterService.getPoojaNakshathraData());
	}

	@PostMapping("/offering")
	public ResponseEntity<?> saveNewOffering(@RequestHeader("Authorization") String token,
			@RequestBody OfferingDto offering) {
		String tenantId = jwt.getTenantId(token);
		String userId = jwt.getUserId(token);
		String transId = SiteHelper.transId("pja");
		Long receiptNo = 0l;
		return ResponseEntity.ok(poojaService.saveOffering(offering, tenantId, userId, transId, receiptNo,1));
	}

	@PostMapping("/offering/bulk")
	public ResponseEntity<List<OfferingDto>> saveBulkOffering(@RequestHeader("Authorization") String token,
			@RequestBody List<OfferingDto> offerings) {
		String tenantId = jwt.getTenantId(token);
		String userId = jwt.getUserId(token);

		List<OfferingDto> savedOfferings = new java.util.ArrayList<>();

		String transId = SiteHelper.transId("pja");

		  String prefix = offerings.get(0)
            .getPoojaTrans()
            .get(0)
            .getPrefix();

    Long nextReceipt = poojaTransactionRepository
            .findMaxReceiptNoByTenantIdAndPrefixAndStatus(tenantId, prefix, BaseStatus.ACTIVE)
            .map(max -> max + 1)
            .orElse(1L);

		int totalPooja = offerings.size();
		for (OfferingDto offering : offerings) {
			OfferingDto saved = poojaService.saveOffering(offering, tenantId, userId, transId, nextReceipt, totalPooja);
			savedOfferings.add(saved);
		}

		return ResponseEntity.ok(savedOfferings);
	}

	@PostMapping("/advance-close")
	public ResponseEntity<ApiResponse> makePayment(@RequestHeader("Authorization") String token,
			@RequestBody PoojaAdvanceCloseRequestDto dto) {
		String tenantId = jwt.getTenantId(token);
		String userId = jwt.getUserId(token);
		try {
			poojaService.closePoojaAdvance(tenantId, userId, dto);
			return ResponseEntity.ok(
					ApiResponse.builder().message("Pooja advance closed successfully").status(HttpStatus.OK).build());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder()
					.message("Failed to close pooja advance").status(HttpStatus.INTERNAL_SERVER_ERROR).build());
		}
	}

	@DeleteMapping("/offering/{tid}")
	public ResponseEntity<ApiResponse> deletePooja(@PathVariable String tid) {
		String tenantId = "abcd";
		poojaService.softDelete(tid, tenantId);
		ApiResponse res = ApiResponse.builder().message("Pooja deleted successfully").status(HttpStatus.OK)
				.build();
		return ResponseEntity.ok(res);
	}
}
