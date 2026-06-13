package com.rahul.kovil.pooja.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rahul.kovil.account.repository.TransactionRepository;
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

	@Autowired
	private TransactionRepository transactionRepository;

	public PoojaService(VendorServiceApi vendorService, PoojaRepository poojaRepository,
			PoojaTransactionRepository poojaTransactionRepository, AccountServiceApi accountService) {
		this.vendorService = vendorService;
		this.accountService = accountService;
		this.poojaRepository = poojaRepository;
		this.poojaTransactionRepository = poojaTransactionRepository;
	}

	@Transactional
	public OfferingDto saveOffering(OfferingDto offering, String tenantId, String userId, String transId,
			Long nextReceipt) {

		// Use injected SiteHelper, not static call
		// String transId = SiteHelper.transId("pja");

		String vendorId = offering.getVendorId();
		Long vendorAccountId = offering.getVendorAccountId();

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

		Pooja pooja = new Pooja(null, vendorId, userId, transId, offering.getBooking(), offering.getBookingDate(), null,
				offering.getAdvanceAmount(), BookingStatus.valueOf(offering.getBookingStatus()),
				offering.getPooja().getDate(), offering.getPooja().getAmount(), offering.getPooja().getPaidAmount(),
				offering.getPooja().getStatus());
		pooja.setTenantId(tenantId);

		// Create transaction list
		List<PoojaTransaction> poojaTrans = new ArrayList<>();

		for (PoojaTransactonDto p : offering.getPoojaTrans()) {
			PoojaTransaction tx = new PoojaTransaction(null, transId, vendorId, p.getPoojaMaster(), p.getPrefix(),
					nextReceipt, p.getAmount(), com.rahul.kovil.common.enums.BaseStatus.ACTIVE);
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

		Long toLedger = offering.getBooking() ? poojaAdvanceLedger : poojaIncomeLedger;
		BigDecimal amount = offering.getBooking() ? offering.getAdvanceAmount() : offering.getPooja().getAmount();
		toLedger = poojaIncomeLedger;
		//
		// TransactionDto transaction = accountService.saveTransaction(new
		// TransactionDto(null, userId, "POOJA", null, vendorAccountId, payMode,
		// amount, transactionDate, offering.getAccRemark(), TransactionType.RECEIPT,
		// TransactionStatus.ACTIVE, transId, offering.getReferenceDate(),
		// offering.getReferneceNo()), tenantId, userId);
		//
		// accountService.saveTransaction(new TransactionDto(null, userId, "POOJA",
		// transaction.getVoucherNo(), toLedger, vendorAccountId,
		// amount, transactionDate, offering.getAccRemark(), TransactionType.RECEIPT,
		// TransactionStatus.ACTIVE, transId, offering.getReferenceDate(),
		// offering.getReferneceNo()), tenantId, userId);

		TransactionDto transaction = accountService.saveTransaction(
				new TransactionDto(null, userId, "POOJA", null, toLedger, payMode,
						amount, transactionDate, offering.getAccRemark(), TransactionType.RECEIPT,
						TransactionStatus.ACTIVE, transId, offering.getReferenceDate(), offering.getReferneceNo()),
				tenantId, userId);

		OfferingDto response = new OfferingDto();
		response.setTransId(transId);
		response.setVendorId(vendorId);
		response.setVendorAccountId(vendorAccountId);
		response.setPooja(offering.getPooja());
		response.setPoojaTrans(offering.getPoojaTrans());

		return response;
	}

	@Transactional
	public void closePoojaAdvance(String tenantId, String userId, PoojaAdvanceCloseRequestDto dto)
			throws RuntimeException {
		Pooja pooja = poojaRepository.findByTransId(dto.getTransId());

		pooja.setBookingStatus(BookingStatus.CLOSED);
		pooja.setBookingCloseDate(dto.getCloseDate());

		String transId = SiteHelper.transId("adv");
		// Long vendorAccountId = dto.getVendorAccId();
		// Long poojaAdvanceLedger = 12l;
		Long poojaIncomeLedger = 11l;
		// LocalDateTime c = dto.getCloseDate().now();
		// accountService.saveTransaction(new TransactionDto(null, userId, "POOJA",
		// null, dto.getPayMode(), vendorAccountId,
		// dto.getPayingAmount(), dto.getCloseDate().atTime(LocalTime.now()),
		// dto.getRemark(), TransactionType.RECEIPT,
		// TransactionStatus.ACTIVE, transId, dto.getReferenceDate(),
		// dto.getReferenceNumber()), tenantId, userId);
		//
		// accountService.saveTransaction(new TransactionDto(null, userId, "POOJA",
		// null, vendorAccountId, poojaIncomeLedger,
		// dto.getPayingAmount(), dto.getCloseDate().atTime(LocalTime.now()),
		// dto.getRemark(), TransactionType.RECEIPT,
		// TransactionStatus.ACTIVE, transId, dto.getReferenceDate(),
		// dto.getReferenceNumber()), tenantId, userId);
		//
		// accountService.saveTransaction(new TransactionDto(null, userId, "POOJA",
		// null, poojaAdvanceLedger, poojaIncomeLedger,
		// dto.getOldAdvance(), dto.getCloseDate().atTime(LocalTime.now()),
		// dto.getRemark(), TransactionType.RECEIPT,
		// TransactionStatus.ACTIVE, transId, dto.getReferenceDate(),
		// dto.getReferenceNumber()), tenantId, userId);

		accountService.saveTransaction(
				new TransactionDto(null, userId, "POOJA", null, poojaIncomeLedger, dto.getPayMode(),
						dto.getPayingAmount(), dto.getCloseDate().atTime(LocalTime.now()), dto.getRemark(),
						TransactionType.RECEIPT,
						TransactionStatus.ACTIVE, transId, dto.getReferenceDate(), dto.getReferenceNumber()),
				tenantId, userId);
	}

	@Transactional
	public void softDelete(String tid, String tenantId) {
		poojaRepository.softDelete(tid, BaseStatus.CANCELED);
		transactionRepository.softDelete(tid, TransactionStatus.CANCELED);
		poojaTransactionRepository.softDelete(tid, BaseStatus.CANCELED);
	}

}
