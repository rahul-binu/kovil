package com.rahul.kovil.report.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.kovil.common.dto.DynamicReportRequest;
import com.rahul.kovil.common.response.ToonResponse;
import com.rahul.kovil.report.service.ReportOrchestratorService;
import com.rahul.kovil.report.service.StaticReport;

@RestController
@RequestMapping("/api/report")
public class ReportController {

	private final ReportOrchestratorService reportService;
	private final StaticReport sreportService;

	public ReportController(ReportOrchestratorService reportService, StaticReport sreportService) {
		this.reportService = reportService;
		this.sreportService = sreportService;
	}

	@PostMapping("/pooja")
	public ResponseEntity<Map<String, ToonResponse>> getPoojaReport(@RequestBody DynamicReportRequest req) {
		return ResponseEntity.ok(reportService.generateReport(req));
	}

	@PostMapping("/s-pooja")
	public ResponseEntity<Map<String, ToonResponse>> getStaticPoojaReport(@RequestBody DynamicReportRequest req) {
		return ResponseEntity.ok(sreportService.getStaticPoojaReport(req));
	}

	@PostMapping("/s-vendor/{vtype}")
	public ResponseEntity<Map<String, ToonResponse>> getStaticVendorReport(@PathVariable String vtype,
			@RequestBody Map<String, String> req) {
		String from = req.get("from");
		String to = req.get("to");
		return ResponseEntity.ok(sreportService.getStaticVendorReport(vtype, from, to));
	}

}
