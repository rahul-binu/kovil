package com.rahul.kovil.account.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rahul.kovil.account.entity.OpeningBalance;
import com.rahul.kovil.common.enums.BaseStatus;

public interface OpeningBalanceRepository extends JpaRepository<OpeningBalance, Long> {

	@Query("SELECT o FROM OpeningBalance o WHERE o.tenantId = :tenantId AND o.status = :status AND o.openingDate BETWEEN :from AND :to")
	List<OpeningBalance> findByTenantIdAndStatusAndOpeningDateBetween(String tenantId, BaseStatus status, LocalDate from,
			LocalDate to);

	@Query("SELECT o.ledgerId, SUM(o.amount) FROM OpeningBalance o WHERE o.status = :status AND o.ledgerId IN :lids AND o.openingDate=:date AND o.tenantId IN :tenantIds GROUP BY o.ledgerId")
	List<Object[]> ledgerOpeningBalance(BaseStatus status, List<Long> lids, LocalDate date, List<String> tenantIds);

	@Query("SELECT MAX(o.openingDate) FROM OpeningBalance o WHERE o.status = :status AND o.tenantId IN :tenantIds")
	LocalDate findMaxOpeningDateByTenantIdInAndStatus(List<String> tenantIds, BaseStatus status);

}
