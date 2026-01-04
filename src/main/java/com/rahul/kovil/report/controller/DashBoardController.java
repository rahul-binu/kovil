package com.rahul.kovil.report.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.kovil.common.response.ToonResponse;
import com.rahul.kovil.config.JwtProvider;
import com.rahul.kovil.report.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashBoardController {

	@Autowired
	private JwtProvider jwt;
	
	@Autowired
	private DashboardService dashboardService;
	
	@GetMapping("/tody/vazhipad")
	public String getMethodName( String param) {
		return new String();
	}
	
	@GetMapping("")
	public ResponseEntity<Map<String, ToonResponse>> dashBordData(@RequestHeader("Authorization") String token) {
		String tenantId = jwt.getTenantId(token);
		return ResponseEntity.ok(dashboardService.getAllDashboardData(tenantId));
	}
	
	
	
}
