package com.rahul.kovil.authentication.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rahul.kovil.common.api.UserServiceApi;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.config.JwtProvider;
import com.rahul.kovil.common.dto.UserDto;

@Service
public class AuthService {
	@Autowired
	private PasswordEncoder passwordEncoder;

	private UserServiceApi userService;

	private final JwtProvider jwtProvider;

	public AuthService(UserServiceApi userService, JwtProvider jwtProvider) {
		this.userService = userService;
		this.jwtProvider = jwtProvider;
	}

	public String authenticate(String userName, String password) {
		UserDto user = userService.getUserByUserName(userName, BaseStatus.ACTIVE);
		
		if (user.getTenantId() == null || user.getTenantId().isBlank()) {
			throw new BadCredentialsException("invalied Username");
		}
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new BadCredentialsException("password not match");
		}
        String tenantId = user.getTenantId();
        String userId = user.getTransId();
        String token = jwtProvider.generateToken(Map.of("userName",  userName, "tenantId", tenantId, "userId", userId));
		
		return token;
	}
}
