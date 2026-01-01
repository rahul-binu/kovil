package com.rahul.kovil.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class VoucherDto {

	private String vtp;
	
	private LocalDate vdt;
	private Long fml;
	private Long tol;
	private BigDecimal amt;
	private String rem;
	private String ren;
	private LocalDate red;
	
	private String userId;
	private String transId;
}