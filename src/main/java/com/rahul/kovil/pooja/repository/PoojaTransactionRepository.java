package com.rahul.kovil.pooja.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.pooja.entity.PoojaTransaction;

@Repository
public interface PoojaTransactionRepository extends JpaRepository<PoojaTransaction, Long> {

	@Query("SELECT pt.prefix AS prefix, COALESCE(MAX(pt.receiptNo), 0) AS maxNo " +
		       "FROM PoojaTransaction pt " +
		       "WHERE pt.tenantId = :tenantId AND pt.prefix IN :prefixes AND pt.status = :status " +
		       "GROUP BY pt.prefix")
		List<Map<String, Object>> findMaxReceiptNoByTenantIdAndPrefixInAndStatus(
		        @Param("tenantId") String tenantId,
		        @Param("prefixes") Set<String> prefixes,
		        @Param("status") BaseStatus status);

}
