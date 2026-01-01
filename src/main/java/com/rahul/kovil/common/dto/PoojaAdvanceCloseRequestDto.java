package com.rahul.kovil.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class PoojaAdvanceCloseRequestDto {
	private String transId;
	private BigDecimal payingAmount;
	private Long payMode;
	private String referenceNumber;
	private LocalDate referenceDate;
	private String remark;
	private BigDecimal oldAdvance;
	private LocalDate closeDate;
	private Long vendorAccId;
}
