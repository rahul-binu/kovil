package com.rahul.kovil.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rahul.kovil.account.entity.Ledger;
import com.rahul.kovil.common.enums.BaseStatus;

import lombok.Data;

@Data
public class OpeningBalanceDto {
	private Long id;
	private Long ledgerId;
	private LocalDate openingDate;
	private BigDecimal amount;
	private BaseStatus status;	
}
