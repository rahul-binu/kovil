package com.rahul.kovil.vendor.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.rahul.kovil.common.dto.LedgerDto;
import com.rahul.kovil.common.dto.QuickVendorDto;
import com.rahul.kovil.common.dto.VendorDto;
import com.rahul.kovil.common.enums.Nakshathra;
import com.rahul.kovil.common.enums.VendorType;
import com.rahul.kovil.common.util.SiteHelper;
import com.rahul.kovil.vendor.entity.Vendor;
import com.rahul.kovil.vendor.repository.VendorRepository;
import com.rahul.kovil.common.api.AccountServiceApi;
import com.rahul.kovil.common.api.VendorServiceApi;

@Service
public class VendorService implements VendorServiceApi{
	
	@Autowired
	private ModelMapper modelMapper;
	
	private final VendorRepository vendorRepository;
	private final AccountServiceApi accountService;
	
	public VendorService(VendorRepository vendorRepository, 
			AccountServiceApi accountService) {
		this.vendorRepository = vendorRepository;
		this.accountService = accountService;
	}
	
	@Override
	public VendorDto saveVendor(VendorDto vendor, String tenantId) {
		return null;
	}
	
	public QuickVendorDto quickCreation(QuickVendorDto vendorDto, String tenantId, String userId) {
		String transId = SiteHelper.transId("ven");
		Vendor vendor = modelMapper.map(vendorDto, Vendor.class);
		vendor.setTransId(transId);
		vendor.setNakshathra(Nakshathra.valueOf(vendorDto.getNakshatra()));
		vendor.setType(VendorType.valueOf(vendorDto.getVendorType()));
		LedgerDto ledger = new LedgerDto(null, vendorDto.getFullName(),12L,"",tenantId, 2, 0);
		
		LedgerDto l = accountService.addLedgerData(ledger, tenantId, userId);
		
		vendor.setTenantId(tenantId);
		vendor.setAccountId(l.getId());
		Vendor savedVendor = vendorRepository.save(vendor);
		return modelMapper.map(savedVendor, QuickVendorDto.class);
	}

	public List<VendorDto> getVendorsByQuery(VendorType type, String query) {
	    
	    List<Vendor> vendors = vendorRepository.findByTypeAndFullNameContainingIgnoreCaseOrTypeAndMobileContainingIgnoreCaseOrTypeAndFamilyNameContainingIgnoreCase(
	    		type, query, type, query, type, query);

	    return vendors.stream()
	            .map(v -> modelMapper.map(v, VendorDto.class))
	            .toList();
	}

	
} 
