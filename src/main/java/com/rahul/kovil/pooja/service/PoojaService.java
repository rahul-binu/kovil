package com.rahul.kovil.pooja.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rahul.kovil.common.api.AccountServiceApi;
import com.rahul.kovil.common.api.VendorServiceApi;
import com.rahul.kovil.common.dto.OfferingDto;
import com.rahul.kovil.common.dto.PoojaAdvanceCloseRequestDto;
import com.rahul.kovil.common.dto.PoojaTransactonDto;
import com.rahul.kovil.common.dto.QuickVendorDto;
import com.rahul.kovil.common.dto.TransactionDto;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.BookingStatus;
import com.rahul.kovil.common.enums.TransactionStatus;
import com.rahul.kovil.common.enums.TransactionType;
import com.rahul.kovil.common.enums.VendorType;
import com.rahul.kovil.common.util.SiteHelper;
import com.rahul.kovil.pooja.entity.Pooja;
import com.rahul.kovil.pooja.entity.PoojaTransaction;
import com.rahul.kovil.pooja.repository.PoojaRepository;
import com.rahul.kovil.pooja.repository.PoojaTransactionRepository;

import jakarta.transaction.Transactional;

@Service
public class PoojaService {

	private final VendorServiceApi vendorService;
	private final AccountServiceApi accountService;

	private final PoojaRepository poojaRepository;
	private final PoojaTransactionRepository poojaTransactionRepository;

	public PoojaService(VendorServiceApi vendorService, PoojaRepository poojaRepository,
			PoojaTransactionRepository poojaTransactionRepository, AccountServiceApi accountService) {
		this.vendorService = vendorService;
		this.accountService = accountService;
		this.poojaRepository = poojaRepository;
		this.poojaTransactionRepository = poojaTransactionRepository;
	}

	@Transactional
	public OfferingDto saveOffering(OfferingDto offering, String tenantId, String userId) {

		// Use injected SiteHelper, not static call
		String transId = SiteHelper.transId("pja");

		String vendorId = offering.getVendorId();
		Long vendorAccountId = 0L;

		LocalDateTime transactionDate = offering.getPooja().getDate();

		// Create vendor if empty
		if (vendorId == null || vendorId.isBlank()) {
			QuickVendorDto vendorDto = new QuickVendorDto(null, null, offering.getVendorName(),
					offering.getVendorPhone(), offering.getVendorFamilyName(), offering.getVenodrAddress(), "",
					offering.getVendorNakshatra(), "DEVOTEE");
			QuickVendorDto vendor = vendorService.quickCreation(vendorDto, tenantId, userId);

			vendorId = vendor.getTransId();
			vendorAccountId = vendor.getAccountId();
		}

		Pooja pooja = new Pooja(null, vendorId, userId, transId, offering.getBooking(), offering.getBookingDate(), null, offering.getAdvanceAmount(), BookingStatus.valueOf(offering.getBookingStatus()), offering.getPooja().getDate(), offering.getPooja().getAmount(), offering.getPooja().getStatus());
		pooja.setTenantId(tenantId);

		// Create transaction list
		List<PoojaTransaction> poojaTrans = new ArrayList<>();

		Set<String> prefixes = offering.getPoojaTrans().stream().map(PoojaTransactonDto::getPrefix)
				.collect(Collectors.toSet());

		List<Map<String, Object>> results = poojaTransactionRepository
				.findMaxReceiptNoByTenantIdAndPrefixInAndStatus(tenantId, prefixes, BaseStatus.ACTIVE);

		Map<String, Long> prefixMaxMap = new HashMap<>();
		for (Map<String, Object> row : results) {
			String prefix = (String) row.get("prefix");
			Number maxNum = (Number) row.get("maxNo"); // cast safely
			Long max = maxNum == null ? 0L : maxNum.longValue();
			prefixMaxMap.put(prefix, max);
		}

		for (String prefix : prefixes) {
			prefixMaxMap.putIfAbsent(prefix, 0L);
		}

		for (PoojaTransactonDto p : offering.getPoojaTrans()) {
			Long nextReceipt = prefixMaxMap.get(p.getPrefix()) + 1;
			prefixMaxMap.put(p.getPrefix(), nextReceipt);

			PoojaTransaction tx = new PoojaTransaction(null, transId, p.getPoojaMaster(), p.getPrefix(), nextReceipt,
					p.getAmount(), p.getStatus());
			tx.setTenantId(tenantId);
			poojaTrans.add(tx);
		}

		// Save entries
		poojaRepository.save(pooja);
		poojaTransactionRepository.saveAll(poojaTrans);

		// accounts entry
		Long payMode = offering.getPaymode();
		Long poojaIncomeLedger = 11l;
		Long poojaAdvanceLedger = 12l;
		
		Long toLedger = offering.getBooking()? poojaAdvanceLedger : poojaIncomeLedger;

		accountService.saveTransaction(new TransactionDto(null, userId, "POOJA", null, payMode, vendorAccountId,
				offering.getPooja().getAmount(), transactionDate, offering.getAccRemark(), TransactionType.RECEIPT,
				TransactionStatus.ACTIVE, transId, offering.getReferenceDate(), offering.getReferneceNo()), tenantId, userId);
		
		accountService.saveTransaction(new TransactionDto(null, userId, "POOJA", null, vendorAccountId, toLedger,
				offering.getPooja().getAmount(), transactionDate, offering.getAccRemark(), TransactionType.RECEIPT,
				TransactionStatus.ACTIVE, transId, offering.getReferenceDate(), offering.getReferneceNo()), tenantId, userId);

		OfferingDto response = new OfferingDto();
		response.setTransId(transId);
		response.setVendorId(vendorId);
		response.setVendorAccountId(vendorAccountId);
		response.setPooja(offering.getPooja());
		response.setPoojaTrans(offering.getPoojaTrans());

		return response;
	}

	
	@Transactional
	public void closePoojaAdvance(String tenantId, String userId, PoojaAdvanceCloseRequestDto dto) throws RuntimeException{
		Pooja pooja = poojaRepository.findByTransId(dto.getTransId());
		
		pooja.setBookingStatus(BookingStatus.CLOSED);
		pooja.setBookingCloseDate(dto.getCloseDate());
		
		String transId = SiteHelper.transId("adv");
		Long vendorAccountId = dto.getVendorAccId();
//		LocalDateTime c = dto.getCloseDate().now();
		
		accountService.saveTransaction(new TransactionDto(null, userId, "POOJA", null, dto.getPayMode(), vendorAccountId,
				dto.getPayingAmount(), dto.getCloseDate().atTime(LocalTime.now()), dto.getRemark(), TransactionType.RECEIPT,
				TransactionStatus.ACTIVE, transId, dto.getReferenceDate(), dto.getReferenceNumber()), tenantId, userId);

		Long poojaIncomeLedger = 11l;
		accountService.saveTransaction(new TransactionDto(null, userId, "POOJA", null, vendorAccountId, poojaIncomeLedger,
				dto.getPayingAmount(), dto.getCloseDate().atTime(LocalTime.now()), dto.getRemark(), TransactionType.RECEIPT,
				TransactionStatus.ACTIVE, transId, dto.getReferenceDate(), dto.getReferenceNumber()), tenantId, userId);
		
		Long poojaAdvanceLedger = 12l;
		accountService.saveTransaction(new TransactionDto(null, userId, "POOJA", null, poojaAdvanceLedger, poojaIncomeLedger,
				dto.getOldAdvance(), dto.getCloseDate().atTime(LocalTime.now()), dto.getRemark(), TransactionType.RECEIPT,
				TransactionStatus.ACTIVE, transId, dto.getReferenceDate(), dto.getReferenceNumber()), tenantId, userId);		
	}


}
