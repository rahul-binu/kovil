package com.rahul.kovil.report.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rahul.kovil.common.api.ReportServiceApi;
import com.rahul.kovil.common.dto.DynamicReportRequest;
import com.rahul.kovil.common.response.ToonResponse;
import com.rahul.kovil.report.util.DynamicReportExtractor;
import com.rahul.kovil.report.util.ReportExtractedPart;

@Service
public class ReportOrchestratorService {

	private final DynamicReportExtractor extractor;

	private final Map<String, ReportServiceApi> reportServices;

	public ReportOrchestratorService(DynamicReportExtractor extractor, Map<String, ReportServiceApi> reportServices) {
		this.extractor = extractor;
		this.reportServices = reportServices;
	}

	/**
	 * Orchestrates all report modules dynamically based on request.
	 */
	public Map<String, ToonResponse> generateReport(DynamicReportRequest req) {

		Map<String, ToonResponse> result = new HashMap<>();
		Map<String, ReportExtractedPart> extracted = extractor.extract(req);

		// *******************************
		// Step 1: Handle Pooja module first
		// *******************************
		if (extracted.containsKey("p")) {
			processPoojaModule(result, extracted);
		}
		// *******************************
		// Step 2: Run all other modules
		// *******************************
		extracted.forEach((alias, part) -> {
			if (!"p".equals(alias)) { // skip Pooja (already executed)
				ReportServiceApi service = reportServices.get(alias);
				if (service != null) {
					ToonResponse response = service.getReport(part.getFields(), part.getFilters(),
							part.getNameFields());
					result.put(alias, response);
				}
			}
		});

		return result;
	}

	/**
	 * Runs Pooja module first and injects its customer IDs into Customer module
	 * filters.
	 */
	private void processPoojaModule(Map<String, ToonResponse> result, Map<String, ReportExtractedPart> extracted) {

		ReportExtractedPart part = extracted.get("p");
		ReportServiceApi pService = reportServices.get("p");

		// Run pooja module and add to final result
		ToonResponse poojaResponse = pService.getReport(part.getFields(), part.getFilters(), part.getNameFields());
		result.put("p", poojaResponse);

		// Get linked customer IDs
		List<Object> customerIds = pService.extractLinkIds();

		// Inject IDs into customer module
		if (!customerIds.isEmpty() && extracted.containsKey("c")) {
			extracted.get("c").getFilters().put("c.id IN", customerIds);
		}
	}
}
