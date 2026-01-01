package com.rahul.kovil.common.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InvStockDto {

	private Long id;
	private Long item;
    private String stockDirection;
    private String transactionType;
    private Double quantity;
    private String transactionUnit;
    private Double unitMultiplier;
    private String remarks;
    private LocalDateTime transactionDate;
}
