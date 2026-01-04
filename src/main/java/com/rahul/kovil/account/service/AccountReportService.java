package com.rahul.kovil.account.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rahul.kovil.account.entity.Ledger;
import com.rahul.kovil.account.repository.LedgerRepository;
import com.rahul.kovil.account.repository.OpeningBalanceRepository;
import com.rahul.kovil.account.repository.TransactionRepository;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.TransactionStatus;
import com.rahul.kovil.common.response.ToonResponse;


@Service
public class AccountReportService {

	private final LedgerRepository ledgerRepository;
	private final TransactionRepository transactionRepository;
	private final OpeningBalanceRepository openingBalanceRepository;
	
	public AccountReportService(LedgerRepository ledgerRepository, TransactionRepository transactionRepository, OpeningBalanceRepository openingBalanceRepository) {
		this.ledgerRepository = ledgerRepository;
		this.transactionRepository = transactionRepository;
		this.openingBalanceRepository = openingBalanceRepository;
	}
	
	
	@Transactional(readOnly = true)
	public Map<String, ToonResponse> daybook(String tenantId, String f, String t){
		Map<String, ToonResponse> res = new HashMap<>();
		
		LocalDateTime from = LocalDate.parse(f).atStartOfDay();
		LocalDateTime to = LocalDate.parse(t).atTime(LocalTime.MAX);
		
		LocalDate openDate = openingBalanceRepository.findMaxOpeningDateByTenantIdInAndStatus(List.of("-1", tenantId), BaseStatus.ACTIVE);
		
		if (openDate == null) {
		    openDate = LocalDate.now();
		}


		List<Object[]> led = ledgerRepository.getIdLedgerNameGroupUnderByTenentIdInAndStatus(List.of("-1", tenantId), BaseStatus.ACTIVE); 
		ToonResponse ledgers = ToonResponse.builder().data(led).label(List.of("id", "nm", "lu")).status("OK").message("all ledger data").build();
		List<Long> cbids = ledgerRepository.ledgerIdsByLedgerUnder(List.of("-1", tenantId), BaseStatus.ACTIVE, List.of(10l ,11l));
		List<Object[]> accOpeng = openingBalanceRepository.ledgerOpeningBalance(BaseStatus.ACTIVE, cbids, openDate ,List.of("-1", tenantId));
		List<Object[]> ledBalan = transactionRepository.currentLedgerBalance(cbids, List.of(TransactionStatus.ACTIVE), List.of("-1", tenantId), openDate.atStartOfDay(), from);
		
		List<Object[]> mopening = new ArrayList<>();

		Map<Long, BigDecimal> ledgerAmountMap = new HashMap<>();
		for (Object[] row : accOpeng) {
		    Long ledgerId = ((Number) row[0]).longValue();
		    BigDecimal amount = row[1] == null ? BigDecimal.ZERO : (BigDecimal) row[1];
		    ledgerAmountMap.merge(ledgerId, amount, BigDecimal::add);
		}

		for (Object[] row : ledBalan) {
		    Long ledgerId = ((Number) row[0]).longValue();
		    BigDecimal amount = row[1] == null ? BigDecimal.ZERO : (BigDecimal) row[1];
		    ledgerAmountMap.merge(ledgerId, amount, BigDecimal::add);
		}

		for (Map.Entry<Long, BigDecimal> entry : ledgerAmountMap.entrySet()) {
		    mopening.add(new Object[]{entry.getKey(), entry.getValue()});
		}	
		ToonResponse opening = ToonResponse.builder().status("OK").message("cash bank opening").data(mopening).label(List.of("id", "amt")).build();
		
		
		List<Object[]> trans = transactionRepository.findByTenantIdAndStatusInAndTransactionDateBetween(tenantId, List.of(TransactionStatus.ACTIVE),from, to);
		ToonResponse transactions = ToonResponse.builder().status("OK").message("full transaction").data(trans).label(
				List.of("usr", "vno", "vtp", "crd", "deb", "amt", "tdt", "rmk", "typ", "rno", "rdt", "tid")).build();
				
		
		res.put("ledger", ledgers);
		res.put("opening", opening);
		res.put("transc", transactions);
		
		return res;
	}
	
	
}
