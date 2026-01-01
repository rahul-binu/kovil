package com.rahul.kovil.common.api;

import com.rahul.kovil.common.dto.QuickVendorDto;
import com.rahul.kovil.common.dto.VendorDto;

public interface VendorServiceApi {
	VendorDto saveVendor(VendorDto vendor, String tenantId);
	
	QuickVendorDto quickCreation(QuickVendorDto vendorDto, String tenantId, String userId);
}
