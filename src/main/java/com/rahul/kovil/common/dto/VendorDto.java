package com.rahul.kovil.common.dto;

import com.rahul.kovil.common.enums.Nakshathra;
import com.rahul.kovil.common.enums.VendorType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorDto {

	private Long Id;
	private Long accountId;
	private String fullName;
	private String mobile;
	private String shopName;
	private String gstNo;
	private String email;
	private String address;
	private String familyName;
	private Long pincode;
	private String state;
	private String transId;
	private VendorType type;
	private Nakshathra nakshathra;
}
