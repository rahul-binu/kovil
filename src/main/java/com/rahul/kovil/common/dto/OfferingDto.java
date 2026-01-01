package com.rahul.kovil.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class OfferingDto {

	private String vendorId;
	private Long vendorAccountId;	
	
	private String vendorName;
	private String vendorPhone;
	private String vendorFamilyName;
	private String venodrAddress;
	private String vendorNakshatra;
	
	private PoojaDto pooja;
	
	private List<PoojaTransactonDto> poojaTrans;
	
	private String transId;
	
	private Long paymode;
	private String accRemark;
	
	private Boolean booking;
	private LocalDate bookingDate;
	private BigDecimal advanceAmount;
	private String bookingStatus;
	
}

