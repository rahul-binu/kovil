package com.rahul.kovil.account.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rahul.kovil.account.entity.Group;
import com.rahul.kovil.account.entity.Ledger;
import com.rahul.kovil.account.entity.OpeningBalance;
import com.rahul.kovil.account.entity.Transaction;
import com.rahul.kovil.account.repository.GroupRepository;
import com.rahul.kovil.account.repository.LedgerRepository;
import com.rahul.kovil.account.repository.OpeningBalanceRepository;
import com.rahul.kovil.account.repository.TransactionRepository;
import com.rahul.kovil.common.api.AccountServiceApi;
import com.rahul.kovil.common.dto.GroupDto;
import com.rahul.kovil.common.dto.LedgerDto;
import com.rahul.kovil.common.dto.OpeningBalanceDto;
import com.rahul.kovil.common.dto.TransactionDto;
import com.rahul.kovil.common.dto.VoucherDto;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.TransactionStatus;
import com.rahul.kovil.common.enums.TransactionType;
import com.rahul.kovil.common.response.ToonResponse;
import com.rahul.kovil.common.util.SiteHelper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class AccountService implements AccountServiceApi {

	@Autowired
	private ModelMapper modelMapper;

	@PersistenceContext
	private EntityManager entityManager;

	private final LedgerRepository ledgerRepository;
	private final GroupRepository groupRepository;
	private final TransactionRepository transactionRepository;
	private final OpeningBalanceRepository openingBalanceRepository;

	public AccountService(LedgerRepository ledgerRepository, TransactionRepository transactionRepository,
			GroupRepository groupRepository, OpeningBalanceRepository openingBalanceRepository) {
		this.ledgerRepository = ledgerRepository;
		this.groupRepository = groupRepository;
		this.transactionRepository = transactionRepository;
		this.openingBalanceRepository = openingBalanceRepository;
	}

	public LedgerDto addLedgerData(LedgerDto ledgerDto, String tenantId, String createdBy) {
		Ledger ledger = modelMapper.map(ledgerDto, Ledger.class);
		ledger.setTenantId(tenantId);
		ledger.setStatus(BaseStatus.ACTIVE);
		ledger.setCreatedUser(createdBy);

		Ledger saved = ledgerRepository.save(ledger);
		return modelMapper.map(saved, LedgerDto.class);
	}

	public LedgerDto updateLedgerData(LedgerDto ledgerDto, String tenantId, String createdBy) {
		Long id = ledgerDto.getId();
		if (id == null) {
			throw new RuntimeException("Ledger ID is required for update.");
		}

		Ledger existing = ledgerRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Ledger not found with ID: " + id));

		if (!existing.getTenantId().equals(tenantId)) {
			throw new RuntimeException("Unauthorized: Ledger does not belong to this tenant.");
		}

		existing.setLedgerName(ledgerDto.getLedgerName());
		existing.setGroupUnder(ledgerDto.getGroupUnder());
		existing.setDescription(ledgerDto.getDescription());
		existing.setAppLock(ledgerDto.getAppLock());
		existing.setOrderNo(ledgerDto.getOrderNo());

		Ledger updated = ledgerRepository.save(existing);
		return modelMapper.map(updated, LedgerDto.class);
	}

	public List<LedgerDto> accountLedgerData(String tenantId, BaseStatus status, List<Integer> lunder) {

		List<LedgerDto> ledgerDtos = new ArrayList<>();

		List<Ledger> ledgers = ledgerRepository.findByTenantIdInAndStatusAndGroupUnderIn(List.of(tenantId, "-1"),
				status, lunder);
		ledgerDtos = ledgers.stream().map(l -> modelMapper.map(l, LedgerDto.class)).toList();
		return ledgerDtos;
	}

	public void softDeleteLedger(Long id, String tenantId, String userId) {
		Ledger ledger = ledgerRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Ledger not found with ID: " + id));
		if (!ledger.getTenantId().equals(tenantId)) {
			throw new RuntimeException("Unauthorized: Ledger does not belong to this tenant");
		}
		ledger.setStatus(BaseStatus.CANCELED);
		ledgerRepository.save(ledger);
	}

	public List<LedgerDto> accountLedgerAllData(String tenantId, BaseStatus status) {
		List<LedgerDto> ledgerDtos = new ArrayList<>();

		List<Ledger> ledgers = ledgerRepository.findByTenantIdInAndStatus(List.of(tenantId, "-1"), status);
		ledgerDtos = ledgers.stream().map(l -> modelMapper.map(l, LedgerDto.class)).toList();
		return ledgerDtos;
	}

	public TransactionDto saveTransaction(TransactionDto transDto, String tenantId, String userId) {
		if(transDto.getAmount().compareTo(new BigDecimal(0)) == 0) throw new RuntimeException("Invalid Amount");
		Transaction transaction = modelMapper.map(transDto, Transaction.class);
		transaction.setTenantId(tenantId);
		transaction.setCreatedUser(userId);
		Long vno = transDto.getVoucherNo();
		Long voucherNo = (vno == null || vno == 0l)? transactionRepository.findMaxVoucherNumberByVoucherTypeAndTenantIdAndStatus(
				transDto.getVoucherType(), tenantId, TransactionStatus.ACTIVE) : vno;
		transaction.setVoucherNo(voucherNo == null ? 1 : voucherNo + 1);
		Transaction savedTransaction = transactionRepository.save(transaction);
		return modelMapper.map(savedTransaction, TransactionDto.class);
	}

	// accounts
	public GroupDto addGroupData(GroupDto groupDto, String tenantId, String createdBy) {
		Group group = modelMapper.map(groupDto, Group.class);
		group.setTenantId(tenantId);
		group.setStatus(BaseStatus.ACTIVE);
		group.setCreatedUser(createdBy);

		Group saved = groupRepository.save(group);
		return modelMapper.map(saved, GroupDto.class);
	}

	public List<GroupDto> accountGroupData(String tenantId, BaseStatus status) {
		List<Group> groups = groupRepository.findByTenanIdInAndStatus(List.of(tenantId, "-1"), status);
		return groups.stream().map(g -> modelMapper.map(g, GroupDto.class)).toList();
	}

	public void softDeleteGroup(Long id, String tenantId) {
		Group group = groupRepository.findById(id).get();
		group.setStatus(BaseStatus.CANCELED);
		groupRepository.save(group);
	}

	public GroupDto updateGroupData(GroupDto groupDto, String tenantId, String updatedBy) {

		Group existing = groupRepository.findById(groupDto.getId())
				.orElseThrow(() -> new RuntimeException("Group not found"));

		existing.setGroupName(groupDto.getGroupName());
		existing.setGroupUnder(groupDto.getGroupUnder());
		existing.setGroupType(groupDto.getGroupType());
		existing.setDescription(groupDto.getDescription());
		existing.setOrderNo(groupDto.getOrderNo());
		existing.setAppLock(groupDto.getAppLock());

		existing.setTenantId(tenantId);
		existing.setCreatedUser(updatedBy);

		Group saved = groupRepository.save(existing);

		return modelMapper.map(saved, GroupDto.class);
	}

	public ToonResponse getLedgerDatas4Voucher(String tenantId, BaseStatus status) {
		List<Object[]> ledgers = ledgerRepository
				.getIdLedgerNameGroupUnderByTenentIdInAndStatus(List.of(tenantId, "-1"), status);

		Set<Long> groupIds = ledgers.stream().map(l -> ((Number) l[2]).longValue()).collect(Collectors.toSet());

		List<Object[]> groups = groupRepository.findGroupTypeIdById(groupIds);

		Map<Long, String> groupTypeMap = groups.stream()
				.collect(Collectors.toMap(g -> ((Number) g[0]).longValue(), g -> (String) g[1]));

		List<Object[]> mergedData = ledgers.stream().map(l -> {
			Long ledgerId = ((Number) l[0]).longValue();
			String ledgerName = (String) l[1];
			Long groupUnder = ((Number) l[2]).longValue();
			String groupType = groupTypeMap.get(groupUnder);

			return new Object[] { ledgerId, ledgerName, groupUnder, groupType };
		}).collect(Collectors.toList());

		List<String> lables = List.of("id", "nm", "gi", "gt");
		return ToonResponse.builder().data(mergedData).status("succes").label(lables).build();
	}

	public VoucherDto addVoucherPayment(VoucherDto voucher, String tenantId, String createdBy) {

		TransactionType type = TransactionType.valueOf(voucher.getVtp());

		String tid = SiteHelper.transId("vou");
		TransactionDto trans = new TransactionDto(null, createdBy, voucher.getVtp() + " VOUCHER", 0L, voucher.getFml(),
				voucher.getTol(), voucher.getAmt(), LocalDateTime.of(voucher.getVdt(), LocalTime.now()),
				voucher.getRem(), type, TransactionStatus.ACTIVE, tid, voucher.getRed(), voucher.getRen());

		TransactionDto savedTrans = saveTransaction(trans, tenantId, createdBy);
		VoucherDto savedVoucher = new VoucherDto();
		savedVoucher.setVtp(voucher.getVtp());
		savedVoucher.setVdt(voucher.getVdt());
		savedVoucher.setFml(voucher.getFml());
		savedVoucher.setTol(voucher.getTol());
		savedVoucher.setAmt(voucher.getAmt());
		savedVoucher.setRem(voucher.getRem());
		savedVoucher.setRen(voucher.getRen());
		savedVoucher.setRed(voucher.getRed());
		savedVoucher.setUserId(createdBy);
		savedVoucher.setTransId(savedTrans.getTransId());

		return savedVoucher;
	}

	public ToonResponse getAllVoucherPayments(String tenantId, List<String> voucherTypes, LocalDateTime from,
			LocalDateTime to) {

		List<String> labels = List.of("cu", // createdUser
				"vn", // voucherNo
				"vt", // voucherType
				"cl", // creditLedger
				"dl", // debitLedger
				"am", // amount
				"td", // transactionDate
				"rm", // remark
				"ty", // type
				"rn", // reference number
				"rd", // reference date
				"tid" // trans id
		);
//		 = List.of("PAYMENT VOUCHER", "RECEIPT VOUCHER", "JOURNAL VOUCHER", "CONTRA VOUCHER");
		List<Object[]> payments = transactionRepository
				.findByTenantIdAndStatusInAndTransactionDateBetweenAndVoucherTypeIn(tenantId,
						List.of(TransactionStatus.ACTIVE), from, to, voucherTypes);

		return ToonResponse.builder().status("sucess").data(payments).label(labels).build();
	}

	@Transactional
	public OpeningBalanceDto saveOpeningBalance(OpeningBalanceDto dto, String tenantId) {

		OpeningBalance entity;

		if (dto.getId() != null) {
			entity = openingBalanceRepository.findById(dto.getId())
					.orElseThrow(() -> new RuntimeException("Opening Balance not found"));
		} else {
			entity = new OpeningBalance();
			entity.setStatus(BaseStatus.ACTIVE);
			entity.setTenantId(tenantId);
		}

		entity.setLedgerId(dto.getLedgerId());
		entity.setOpeningDate(dto.getOpeningDate());
		entity.setAmount(dto.getAmount());

		OpeningBalance saved = openingBalanceRepository.save(entity);
		return modelMapper.map(saved, OpeningBalanceDto.class);
	}

	public void deleteOpeningBalance(Long id) {
		OpeningBalance entity = openingBalanceRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Opening Balance not found"));
		entity.setStatus(BaseStatus.CANCELED);
		openingBalanceRepository.save(entity);
	}

	public List<OpeningBalanceDto> getAccountOpeningBalance(String tenatId, LocalDate f, LocalDate t) {
		List<OpeningBalance> openings = openingBalanceRepository.findByTenantIdAndStatusAndOpeningDateBetween(tenatId,
				BaseStatus.ACTIVE, f, t);
		List<OpeningBalanceDto> openingDtos = openings.stream().map(o -> modelMapper.map(o, OpeningBalanceDto.class))
				.toList();
		return openingDtos;
	}

	public void softDeleteVoucher(String id, String tenantId) {
		Transaction transaction = transactionRepository.findByTransId(id);
		transaction.setStatus(TransactionStatus.CANCELED);
		transactionRepository.save(transaction);
	}

}
