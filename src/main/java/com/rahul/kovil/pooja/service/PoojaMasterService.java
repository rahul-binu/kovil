package com.rahul.kovil.pooja.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rahul.kovil.account.entity.Ledger;
import com.rahul.kovil.account.repository.LedgerRepository;
import com.rahul.kovil.common.dto.PoojaAdvanceCloseRequestDto;
import com.rahul.kovil.common.dto.PoojaMasterDto;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.Nakshathra;
import com.rahul.kovil.pooja.entity.PoojaMaster;
import com.rahul.kovil.pooja.repository.PoojaMasterRepository;
import com.rahul.kovil.pooja.repository.PoojaRepository;

@Service
public class PoojaMasterService {

	@Autowired
	private ModelMapper modelMapper;

	private final PoojaMasterRepository poojaMasterRepository;

	private final LedgerRepository ledgerRepository;

	public PoojaMasterService(PoojaMasterRepository poojaMaster, LedgerRepository ledgerRepository) {
		poojaMasterRepository = poojaMaster;
		this.ledgerRepository = ledgerRepository;
	}

	public PoojaMasterDto savePoojaMaster(PoojaMasterDto poojaDto, String tenantId) {
		PoojaMaster pooja = modelMapper.map(poojaDto, PoojaMaster.class);
		pooja.setTenantId(tenantId);
		Ledger ledger = new Ledger();

		ledger.setTenantId(tenantId);
		ledger.setLedgerName(pooja.getName());
		ledger.setAppLock(0);
		ledger.setCreatedUser("-1");
		ledger.setOrderNo(0);
		ledger.setGroupUnder(13l);
		ledger.setDescription(pooja.getName() + " Income");
		ledger.setStatus(BaseStatus.ACTIVE);

		Ledger savedLedger = ledgerRepository.save(ledger);
		pooja.setLedgerId(savedLedger.getId());
		PoojaMaster saved = poojaMasterRepository.save(pooja);
		return modelMapper.map(saved, PoojaMasterDto.class);
	}

	public List<PoojaMasterDto> getPoojaMasterData(String tenantId, BaseStatus status) {
		List<PoojaMaster> entities = poojaMasterRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId,
				status);

		return entities.stream().map(p -> modelMapper.map(p, PoojaMasterDto.class)).collect(Collectors.toList());
	}

	public void softDelete(Long id, String tenantId) {
		PoojaMaster pooja = poojaMasterRepository.findByIdAndTenantId(id, tenantId)
				.orElseThrow(() -> new RuntimeException("No pooja found"));
		pooja.setStatus(BaseStatus.CANCELED);
		poojaMasterRepository.save(pooja);
	}

	public PoojaMasterDto updatePoojaMaster(PoojaMasterDto poojaDto, String tenantId) {
		PoojaMaster pooja = poojaMasterRepository.findByIdAndTenantId(poojaDto.getId(), tenantId)
				.orElseThrow(() -> new RuntimeException("No pooja found with ID: " + poojaDto.getId()));

		pooja.setName(poojaDto.getName());
		pooja.setGroupName(poojaDto.getGroupName());
		pooja.setDescription(poojaDto.getDescription());
		pooja.setAmount(poojaDto.getAmount());
		pooja.setSpecialAmount(poojaDto.getSpecialAmount());
		pooja.setVariableRate(poojaDto.isVariableRate());
		pooja.setDurationInMinutes(poojaDto.getDurationInMinutes());
		pooja.setOnlyOnSpecificDays(poojaDto.isOnlyOnSpecificDays());
		pooja.setAllowedDays(poojaDto.getAllowedDays());
		pooja.setRequiresBookingDate(poojaDto.isRequiresBookingDate());
		pooja.setCutoffTime(poojaDto.getCutoffTime());
		pooja.setRequiresNakshathra(poojaDto.isRequiresNakshathra());
		pooja.setRequiresGothra(poojaDto.isRequiresGothra());
		pooja.setTokenLimitPerDay(poojaDto.getTokenLimitPerDay());
		pooja.setMaterialsList(poojaDto.getMaterialsList());
		pooja.setDisplayOrder(poojaDto.getDisplayOrder());

		PoojaMaster updatedPooja = poojaMasterRepository.save(pooja);

		return modelMapper.map(updatedPooja, PoojaMasterDto.class);
	}

	public List<String> getPoojaNakshathraData() {
		return Arrays.stream(Nakshathra.values()).map(Enum::name).toList();
	}

}
