package com.rahul.kovil.pooja.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/vazhipad")
public class VazhipadController {

	public VazhipadController() {
		
	}
	
	@GetMapping("")
	public String vazhipadView(Model m) {
		m.addAttribute("today", LocalDate.now());
		return "modules/pooja/vazhipad";
	}
	
}
