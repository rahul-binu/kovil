package com.rahul.kovil.account.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.kovil.config.JwtProvider;
import com.rahul.kovil.account.service.AccountReportService;
import com.rahul.kovil.account.service.AccountService;
import com.rahul.kovil.common.dto.GroupDto;
import com.rahul.kovil.common.dto.LedgerDto;
import com.rahul.kovil.common.dto.OpeningBalanceDto;
import com.rahul.kovil.common.dto.VoucherDto;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.response.ApiResponse;
import com.rahul.kovil.common.response.ToonResponse;
import com.rahul.kovil.common.util.SiteHelper;

@RestController
@RequestMapping("/api/account")
public class AccountController {

	private final JwtProvider jwt;

	private final AccountService accountService;
	private final AccountReportService accountReportService;

	public AccountController(JwtProvider jwt, AccountService accountService,
			AccountReportService accountReportService) {
		this.jwt = jwt;
		this.accountService = accountService;
		this.accountReportService = accountReportService;
	}

	@GetMapping("/ledgers")
	public ResponseEntity<List<LedgerDto>> getLedgerData(@RequestHeader("Authorization") String token,
			@RequestParam List<Integer> under) {
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.ok(accountService.accountLedgerData(tenantId, BaseStatus.ACTIVE, under));
	}

	@PostMapping("/ledger")
	public ResponseEntity<ApiResponse> addLedgerData(@RequestHeader("Authorization") String token,
			@RequestBody LedgerDto ledgerDto) {
		String createdBy = jwt.getUserId(token);
		String tenantId = jwt.getTenantId(token);
		accountService.addLedgerData(ledgerDto, tenantId, createdBy);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.builder().status(HttpStatus.OK).message("Ledger created successfully").build());
	}

	@PutMapping("/ledger")
	public ResponseEntity<ApiResponse> updateLedgerData(@RequestHeader("Authorization") String token,
			@RequestBody LedgerDto ledgerDto) {
		String createdBy = jwt.getUserId(token);
		String tenantId = jwt.getTenantId(token);
		accountService.updateLedgerData(ledgerDto, tenantId, createdBy);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.builder().status(HttpStatus.OK).message("Ledger modified successfully").build());
	}

	@DeleteMapping("/ledger/{id}")
	public ResponseEntity<ApiResponse> deleteLedger(@RequestHeader("Authorization") String token,
			@PathVariable Long id) {
		String tenantId = jwt.getTenantId(token);
		String userId = jwt.getUserId(token);
		accountService.softDeleteLedger(id, tenantId, userId);
		return ResponseEntity
				.ok(ApiResponse.builder().status(HttpStatus.OK).message("Ledger deleted successfully").build());
	}

	@GetMapping("/all-ledgers")
	public ResponseEntity<List<LedgerDto>> getLedgerDatas(@RequestHeader("Authorization") String token) {
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.ok(accountService.accountLedgerAllData(tenantId, BaseStatus.ACTIVE));
	}

	@GetMapping("/all-ledgers4voucher")
	public ResponseEntity<ToonResponse> getLedgerDatas4Voucher(@RequestHeader("Authorization") String token) {
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.ok(accountService.getLedgerDatas4Voucher(tenantId, BaseStatus.ACTIVE));
	}
	// group

	@PostMapping("/group")
	public ResponseEntity<GroupDto> saveGroup(@RequestHeader("Authorization") String token,
			@RequestBody GroupDto groupDto) {
		String tenantId = jwt.getTenantId(token);
		String createdBy = jwt.getUserId(token);
		GroupDto savedGroup = accountService.addGroupData(groupDto, tenantId, createdBy);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedGroup);
	}

	@GetMapping("/groups")
	public ResponseEntity<List<GroupDto>> getGroupData(@RequestHeader("Authorization") String token) {
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.ok(accountService.accountGroupData(tenantId, BaseStatus.ACTIVE));
	}

	@PutMapping("/group")
	public ResponseEntity<GroupDto> updateGroup(@RequestHeader("Authorization") String token,
			@RequestBody GroupDto groupDto) {

		String tenantId = jwt.getTenantId(token);
		String updatedBy = jwt.getUserId(token);

		GroupDto updatedGroup = accountService.updateGroupData(groupDto, tenantId, updatedBy);

		return ResponseEntity.ok(updatedGroup);
	}

	@DeleteMapping("/group/{id}")
	public ResponseEntity<ApiResponse> deletrGroup(@RequestHeader("Authorization") String token,
			@PathVariable Long id) {
		try {
			String tenantId = jwt.getTenantId(token);
			accountService.softDeleteGroup(id, tenantId);
			return ResponseEntity
					.ok(ApiResponse.builder().status(HttpStatus.OK).message("Group deleted successfully").build());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
					ApiResponse.builder().status(HttpStatus.INTERNAL_SERVER_ERROR).message(e.getMessage()).build());
		}

	}

	// voucher
	@PostMapping("/voucher")
	public ResponseEntity<VoucherDto> saveVoucher(@RequestHeader("Authorization") String token,
			@RequestBody VoucherDto voucher) {
		String tenantId = jwt.getTenantId(token);
		String createdBy = jwt.getUserId(token);
		VoucherDto savedGroup = accountService.addVoucherPayment(voucher, tenantId, createdBy);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedGroup);
	}

	@GetMapping("/voucher-payments/{from}/{to}/{vtype}")
	public ResponseEntity<ToonResponse> getAllVoucherPayments(@RequestHeader("Authorization") String token,
			@PathVariable String from, @PathVariable String to, @PathVariable("vtype") List<String> voucherTypes) {
		String tenantId = jwt.getTenantId(token);
		LocalDateTime fromDt = SiteHelper.toLocalDateTime(from, false);
		LocalDateTime toDt = SiteHelper.toLocalDateTime(to, true);
		ToonResponse response = accountService.getAllVoucherPayments(tenantId, voucherTypes, fromDt, toDt);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/voucher/{id}")
	public ResponseEntity<ApiResponse> deleteVoucher(@RequestHeader("Authorization") String token,
			@PathVariable String id) {
		try {
			String tenantId = jwt.getTenantId(token);
			accountService.softDeleteVoucher(id, tenantId);
			return ResponseEntity
					.ok(ApiResponse.builder().status(HttpStatus.OK).message("Group deleted successfully").build());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
					ApiResponse.builder().status(HttpStatus.INTERNAL_SERVER_ERROR).message(e.getMessage()).build());
		}

	}

	// opening balance
	@GetMapping("/opening-balance/{from}/{to}")
	public ResponseEntity<List<OpeningBalanceDto>> getAccountOpeningBalance(
			@RequestHeader("Authorization") String token, @PathVariable String from, @PathVariable String to) {
		String tenatId = jwt.getTenantId(token);
		LocalDate f = LocalDate.parse(from);
		LocalDate t = LocalDate.parse(to);
		return ResponseEntity.ok(accountService.getAccountOpeningBalance(tenatId, f, t));
	}

	@PostMapping("/opening-balance")
	public ResponseEntity<OpeningBalanceDto> saveOpeningBalance(@RequestHeader("Authorization") String token,
			@RequestBody OpeningBalanceDto dto) {
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.status(HttpStatus.CREATED).body(accountService.saveOpeningBalance(dto, tenantId));
	}

	@DeleteMapping("/opening-balance")
	public ResponseEntity<ApiResponse> deleteOpeningBalance(@RequestParam Long id) {
		accountService.deleteOpeningBalance(id);
		return ResponseEntity.status(HttpStatus.OK).body(
				ApiResponse.builder().status(HttpStatus.OK).message("Opening balance cancelled successfully").build());
	}

	// report
	@GetMapping("/daybook/{from}/{to}")
	public ResponseEntity<Map<String, ToonResponse>> daybook(@RequestHeader("Authorization") String token,
			@PathVariable String from, @PathVariable String to) {
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.ok(accountReportService.daybook(tenantId, from, to));
	}

}
