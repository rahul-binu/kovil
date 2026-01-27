package com.rahul.kovil.report.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.BookingStatus;
import com.rahul.kovil.common.response.ToonResponse;
import com.rahul.kovil.pooja.entity.Pooja;
import com.rahul.kovil.pooja.entity.PoojaTransaction;
import com.rahul.kovil.pooja.repository.PoojaRepository;
import com.rahul.kovil.pooja.repository.PoojaTransactionRepository;
import com.rahul.kovil.vendor.entity.Vendor;
import com.rahul.kovil.vendor.repository.VendorRepository;

@Service
public class DashboardService {
	
	@Autowired
	private PoojaTransactionRepository poojaTransactionRepository;
	
	@Autowired 
	private PoojaRepository poojaRepository;
	
	@Autowired
	private VendorRepository vendorRepository;

	public Map<String, ToonResponse> getAllDashboardData(String tenantId) {
		Map<String, ToonResponse> res = new HashMap<>();
		
		res.put("ypooja", monthPoojaCompleted(tenantId, LocalDateTime.now().minusYears(1), LocalDateTime.now()));
		res.put("yvend", monthDevoteeRegistar(tenantId, LocalDateTime.now().minusYears(1), LocalDateTime.now()));
		
		res.put("tpooja", todyPoojaFrequency(tenantId, LocalDateTime.now().minusYears(1), LocalDateTime.now()));
		
		return res;
	}
	
	public ToonResponse monthPoojaCompleted(String tenantId, LocalDateTime fromDate, LocalDateTime toDate) {
		List<Object[]> pooja = poojaTransactionRepository.findNoOfPoojaCompletedByMonth(fromDate, toDate, BaseStatus.ACTIVE, tenantId);
		ToonResponse res = ToonResponse.builder().message("pooja").status("success").data(pooja).label(List.of("month", "no")).build();
		return res;
	}
	
	public ToonResponse monthDevoteeRegistar(String tenantId, LocalDateTime fromDate, LocalDateTime toDate) {
		List<Object[]> pooja = vendorRepository.findNoOfDevoteeRegistarByMonth(fromDate, toDate, BaseStatus.ACTIVE, tenantId);
		ToonResponse res = ToonResponse.builder().message("vendor").status("success").data(pooja).label(List.of("month", "no")).build();
		return res;
	}
	
	
	public ToonResponse todyPoojaFrequency(String tenantId, LocalDateTime fromDate, LocalDateTime toDate) {
		List<Object[]> pooja = poojaTransactionRepository.findTodayPoojaBySession(fromDate, toDate, BaseStatus.ACTIVE, tenantId);
		ToonResponse res = ToonResponse.builder().message("Todays pooja ").status("success").data(pooja).label(List.of("session", "no")).build();
		return res;
	}
	
	public Map<String, Object> poojaBookingsByDate(String tenantId, LocalDate date) {
		Map<String, Object> res = new HashMap<>();
		
		List<Pooja> pooja = poojaRepository.findPoojaBookingByDate(date, BaseStatus.ACTIVE, tenantId, BookingStatus.ACTIVE);
		
		List<String> tids = pooja.stream().map(p->p.getTransId()).toList();
		List<String> vids = pooja.stream().map(p->p.getDevotee()).toList();
		
		List<PoojaTransaction> pts = poojaTransactionRepository.findByTransIdIn(tids);
		List<Vendor> vend = vendorRepository.findByTransIdIn(vids);
		
		res.put("v", vend);
		res.put("pj", pooja);
		res.put("pt", pts);
		
		return res;
	}

}
