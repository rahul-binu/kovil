package com.rahul.kovil.common.api;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.dto.UserDto;

public interface UserServiceApi {
	UserDto saveUser(UserDto userDto, String tenantId);

	UserDto getUserByUserName(String userName, BaseStatus status);

	void softDeleteLedger(String tenantId, String userId);
	
}
