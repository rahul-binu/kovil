package com.rahul.kovil.user.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.rahul.kovil.common.api.UserServiceApi;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.response.ApiResponse;
import com.rahul.kovil.common.response.DataResponse;
import com.rahul.kovil.config.JwtProvider;
import com.rahul.kovil.common.dto.UserDto;

@RestController
@RequestMapping("/api/user")
public class UserController {
	
	private final JwtProvider jwt;
	
	private final UserServiceApi userService;
	
	public UserController(UserServiceApi userService, JwtProvider jwt) {
		this.userService = userService;
		this.jwt = jwt;
	}
	
	@PostMapping("")
	public ResponseEntity<DataResponse> createUser(@RequestHeader("Authorization") String token ,@RequestBody UserDto userDto) {
		String tenatnId = jwt.getTenantId(token);
		if(userService.getUserByUserName(userDto.getUserName(), BaseStatus.ACTIVE) != null)
			throw new RuntimeException("This user name is already taken");
		UserDto user = userService.saveUser(userDto, tenatnId);
		DataResponse res = DataResponse.builder().timestamp(LocalDateTime.now()).status("succes").data(user).build();
		return ResponseEntity.status(HttpStatus.CREATED).body(res);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> deleteUser(@RequestHeader("Authorization") String token,@PathVariable("id") String userId){
		String tenantId = jwt.getTenantId(token);
		userService.softDeleteLedger(tenantId, userId);
		ApiResponse response = ApiResponse.builder()
	            .timestamp(LocalDateTime.now())
	            .status(HttpStatus.OK)
	            .message("User deleted successfully")
	            .build();
		return ResponseEntity.ok(response);
	}
	
}
