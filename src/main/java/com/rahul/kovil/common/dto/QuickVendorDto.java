package com.rahul.kovil.common.dto;

import com.rahul.kovil.common.enums.VendorType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuickVendorDto {

	private Long Id;
	private Long accountId;
	private String fullName;
	private String mobile;
	private String familyName;
	private String address;
	private String transId;
	private String nakshatra;
	private String vendorType;
}
