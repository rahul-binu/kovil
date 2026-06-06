package com.rahul.kovil.pooja.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rahul.kovil.pooja.entity.Pooja;
import com.rahul.kovil.pooja.entity.PoojaTransaction;
import com.rahul.kovil.pooja.repository.PoojaRepository;
import com.rahul.kovil.pooja.repository.PoojaTransactionRepository;
import com.rahul.kovil.vendor.entity.Vendor;
import com.rahul.kovil.vendor.repository.VendorRepository;

@Service
public class PPrintService {

	private final PoojaRepository poojaRepository;
	private final PoojaTransactionRepository poojaTransactionRepository;
	private final VendorRepository vendorRepository;
	
	
	public PPrintService(PoojaRepository poojaRepository, PoojaTransactionRepository poojaTransactionRepository, VendorRepository vendorRepository) {
		this.poojaRepository = poojaRepository;
		this.poojaTransactionRepository = poojaTransactionRepository;
		this.vendorRepository = vendorRepository;
	}
	
	public Map<String, Object> poojaRreceiptPrint(String tid) {
		Map<String, Object> res = new HashMap<>();
		
		Pooja pooja = poojaRepository.findByTransId(tid);
		List<PoojaTransaction> poojat = poojaTransactionRepository.findByTransId(tid);
		Vendor vendor = vendorRepository.findByTransId(pooja.getDevotee());		
		
		res.put("pooja", pooja);
		res.put("poojat", poojat);
		res.put("vendor", vendor);
		
		return res;
	}

}
