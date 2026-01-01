package com.rahul.kovil.authentication.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.kovil.config.JwtProvider;
import com.rahul.kovil.authentication.request.LoginRequest;
import com.rahul.kovil.authentication.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final JwtProvider jwtProvider;

	private final AuthService authService;

	public AuthController(JwtProvider jwtProvider, AuthService authService) {
		this.jwtProvider = jwtProvider;
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<String> login(@ModelAttribute LoginRequest loginRequest) {
		try {
			String tenantId = authService.authenticate(loginRequest.getUserName(), loginRequest.getPassword());
			String token = jwtProvider.generateToken(Map.of("userName",  loginRequest.getUserName(), "tenantId", tenantId));
			return ResponseEntity.ok(token);
		} catch (BadCredentialsException ex) {
			return ResponseEntity.ok("Something went wrong");
		}
	}
	
	
	
	
	
}
