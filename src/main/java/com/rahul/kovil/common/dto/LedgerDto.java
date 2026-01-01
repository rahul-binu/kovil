package com.rahul.kovil.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LedgerDto {
	private Long id;
	private String ledgerName;
	private Long groupUnder;
	private String description;
	private String tenantId;
	private Integer appLock;
	private Integer orderNo;
}
