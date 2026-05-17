package com.rahul.kovil.pooja.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.kovil.pooja.service.PrintService;

@RestController
@RequestMapping("/api/print")
public class PrintController {
	
	private final PrintService printService;
	
	public PrintController(PrintService printService) {
		this.printService = printService;
	}
	
	@GetMapping("/pooja/{tids}")
	public ResponseEntity<?> poojaRreceiptPrint(@PathVariable String tids){
		if (tids != null && tids.contains(",")) {
			String[] splitTids = tids.split(",");
			java.util.List<Object> results = new java.util.ArrayList<>();
			for (String tid : splitTids) {
				results.add(printService.poojaRreceiptPrint(tid.trim()));
			}
			return ResponseEntity.ok(results);
		}
		return ResponseEntity.ok(printService.poojaRreceiptPrint(tids));
	}
}
