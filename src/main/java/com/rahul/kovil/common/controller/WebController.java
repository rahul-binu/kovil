package com.rahul.kovil.common.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rahul.kovil.authentication.request.LoginRequest;
import com.rahul.kovil.authentication.service.AuthService;
import com.rahul.kovil.common.enums.ItemTransactionType;
import com.rahul.kovil.common.enums.ItemUnitType;
import com.rahul.kovil.common.util.LicenseStatus;
import com.rahul.kovil.common.util.LicenseValidation;
import com.rahul.kovil.config.JwtProvider;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/web")
public class WebController {

	private final JwtProvider jwtProvider;

	private final AuthService authService;

    private final LicenseValidation licenseService;
    
    @Value("${client.name}")
    private String clientName="";
    @Value("${client.address}")
    private String clientAddress = "";
    
	public WebController(JwtProvider jwtProvider, AuthService authService, LicenseValidation licenseService) {
		this.jwtProvider = jwtProvider;
		this.authService = authService;
        this.licenseService = licenseService;
	}

    @GetMapping("/license/validate")
    @ResponseBody
    public LicenseStatus check() {
        return licenseService.checkLicense();
    }
    
	// auth/dash
	@GetMapping("/auth/login")
	public String loginForm() {
		return "modules/authentication/login";
	}

	@GetMapping("/auth/dashboard")
	public String dashboard() {
		return "modules/others/dashboard";
	}

	@PostMapping("/auth/login")
	public String login(@ModelAttribute LoginRequest loginRequest, Model model) {
		try {
			String token = authService.authenticate(loginRequest.getUserName(), loginRequest.getPassword());
			model.addAttribute("jwtToken", token);
			return "modules/authentication/store-token";
		} catch (BadCredentialsException ex) {
			model.addAttribute("error", "Invalid username or password");
			return "modules/authentication/login";
		}
	}

	// pooja master

	@GetMapping("/pooja/master")
	public String poojaMaster() {
		return "modules/pooja/poojamaster";
	}

	@GetMapping("/pooja/receipt/{id}/{tid}")
	public String poojaHtmlPrint(@PathVariable int id, @PathVariable String tid, Model model) {
		model.addAttribute("transid", tid);
		if(id == 1) {
			return "modules/pooja/print/receiptprint1";
		}
		return "modules/pooja/print/receiptprint";
	}
	
	// reports
	@GetMapping("/report")
	public String poojaReportDashboard(Model model) {
		return "modules/report/report_dashboard";
	}
	
	@GetMapping("/report/pooja")
	public String poojaReport(Model model) {
		model.addAttribute("fo", LocalDate.now().minusDays(1));
		model.addAttribute("to", LocalDate.now());
		return "modules/report/pooja/pooja";
	}
	
	@GetMapping("/report/pooja-advance")
	public String poojaAdvanceReport(Model model) {
		model.addAttribute("fo", LocalDate.now().minusDays(1));
		model.addAttribute("to", LocalDate.now());
		return "modules/report/pooja/pooja_advance";
	}
	
	@GetMapping("/report/a-pooja")
	public String allPoojaReport(Model model) {
		model.addAttribute("fo", LocalDate.now().minusDays(1));
		model.addAttribute("to", LocalDate.now());
		return "modules/report/pooja/a_pooja";
	}
	
	// accounts 
	@GetMapping("/accounts/ledgers")
	public String accountLedger() {
		return "modules/account/ledger";
	}
	
	@GetMapping("/accounts/groups")
	public String accountGroup() {
		return "modules/account/group";
	}
	
	@GetMapping("/accounts/vouchers/{type}")
	public String accountVoucher(@PathVariable String type,Model model) {
		model.addAttribute("vtype", type);
		model.addAttribute("today", LocalDate.now());
		model.addAttribute("clientName", clientName);
		model.addAttribute("clientAddress", clientAddress);
		return "modules/account/voucher";
	}
	
	@GetMapping("/accounts/opening-balance")
	public String accountOpeningBalance(Model model) {
		model.addAttribute("today", LocalDate.now());
		return "modules/account/account_opening";
	}
	
	@GetMapping("/accounts/daybook")
	public String daybook(Model model) {
		model.addAttribute("today", LocalDate.now());
		return "modules/account/daybook";
	}
	
	// devotee
	@GetMapping("/vendor/{vtype}")
	public String vendorPage(@PathVariable String vtype, Model model) {
		model.addAttribute("from", LocalDate.now());
		model.addAttribute("to", LocalDate.now());
		model.addAttribute("vtype", vtype);
		return "modules/others/vendor";
	}
	
	// inventory
	@GetMapping("/inventory/item")
	public String itemMaster(Model model) {
		model.addAttribute("utype", ItemUnitType.values());
		return "modules/inventory/item";
	}
	
	@GetMapping("/inventory/stock")
	public String currentStock(Model model) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		model.addAttribute("dateTime", LocalDateTime.now().format(formatter));
		return "modules/inventory/current_stock";
	}
	
	@GetMapping("/inventory/stock-transaction")
	public String stockTransaction(Model model) {
		model.addAttribute("date", LocalDate.now());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		model.addAttribute("dateTime", LocalDateTime.now().format(formatter));
		model.addAttribute("transType", ItemTransactionType.values());
		model.addAttribute("utype", ItemUnitType.values());
		return "modules/inventory/stock_transaction";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
