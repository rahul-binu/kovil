package com.rahul.kovil.account.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rahul.kovil.account.entity.Ledger;
import com.rahul.kovil.common.enums.BaseStatus;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, Long> {

	List<Ledger> findByTenantIdInAndStatusAndGroupUnderIn(List<String> tenantIds, BaseStatus status, List<Integer> lunder);

	List<Ledger> findByTenantIdInAndStatus(List<String> tenantIds, BaseStatus status);

	
	@Query("SELECT l.id, l.ledgerName, l.groupUnder FROM Ledger l WHERE l.tenantId IN :tenantId AND l.status = :status")
	List<Object[]> getIdLedgerNameGroupUnderByTenentIdInAndStatus(List<String> tenantId, BaseStatus status);

	@Query("SELECT l.id FROM Ledger l WHERE l.tenantId IN :tenantId AND l.status = :status AND l.groupUnder IN :lunder")
	List<Long> ledgerIdsByLedgerUnder(List<String> tenantId, BaseStatus status, List<Long> lunder);

}
