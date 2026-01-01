package com.rahul.kovil.inventory.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.inventory.entity.StockTransaction;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

//	@Query("""
//			SELECT s1.item, SUM(s1.quantity) FROM StockTransaction s1
//				WHERE s1.tenantId IN :tenantIds
//					AND s1.transactionDate <= :tillDate
//					AND s1.status IN :status
//					AND s1.stockDirection = "IN"
//				GROUP BY item
//
//			UNION ALL
//
//			SELECT s1.item, -SUM(s1.quantity) FROM StockTransaction s1
//				WHERE s1.tenantId IN :tenantIds
//					AND s1.transactionDate <= :tillDate
//					AND s1.status IN :status
//					AND s1.stockDirection = "OUT"
//				GROUP BY item
//			""")
//	List<Object[]> findCurrentStockByTransactionDateAndTenantId(LocalDateTime tillDate, List<String> tenantIds,
//			List<BaseStatus> status);

	@Query("""
			SELECT
			    s.item,
			    SUM(
			    	CASE
			             WHEN s.stockDirection = 'IN'
			                 THEN (s.quantity * s.unitMultiplier)
			             ELSE
			                 -(s.quantity * s.unitMultiplier)
			         END
			     )
			FROM StockTransaction s
			WHERE s.tenantId IN :tenantIds
			  AND s.transactionDate <= :tillDate
			  AND s.status IN :status
			GROUP BY s.item

						""")
	List<Object[]> findCurrentStockByTransactionDateAndTenantId(LocalDateTime tillDate, List<String> tenantIds,
			List<BaseStatus> status);

	@Query("""
			SELECT s.item, s.stockDirection, s.transactionType, s.quantity, s.transactionUnit, s.unitMultiplier, s.remarks, s.transactionDate
			FROM StockTransaction s
			WHERE s.transactionDate >= :fromDate
			  AND s.transactionDate <= :toDate
			  AND s.status IN :status
			  AND s.tenantId IN :tenantIds
			ORDER BY s.transactionDate
			""")
	List<Object[]> findStockTransactionBetweenTransactionDateAndTenantId(LocalDateTime fromDate, LocalDateTime toDate,
			List<String> tenantIds, List<BaseStatus> status);
}
