package com.rahul.kovil.common.dto;

import com.rahul.kovil.common.enums.BaseStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
	private Long id;
	private String transId;
	private String tenantId;
	private String userName;
	private String password;
	private String fullName;
	private String email;
	private String mobileNo;
	private String address;
	private String profilePicture;
	private BaseStatus status;
	
	
}