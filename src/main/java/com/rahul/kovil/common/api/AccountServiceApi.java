package com.rahul.kovil.common.api;

import java.util.List;

import com.rahul.kovil.common.dto.LedgerDto;
import com.rahul.kovil.common.dto.TransactionDto;
import com.rahul.kovil.common.enums.BaseStatus;

public interface AccountServiceApi {
	LedgerDto addLedgerData(LedgerDto ledgerDto, String tenantId, String createdBy);
	
	List<LedgerDto> accountLedgerData(String tenantId, BaseStatus status, List<Integer> lunder);
	
	TransactionDto saveTransaction(TransactionDto transDto, String tenantId, String userId);
}
