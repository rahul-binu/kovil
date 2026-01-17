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
	
	@GetMapping("/pooja/{tid}")
	public ResponseEntity<?> poojaRreceiptPrint(@PathVariable String tid){
		return ResponseEntity.ok(printService.poojaRreceiptPrint(tid));
	}
}
