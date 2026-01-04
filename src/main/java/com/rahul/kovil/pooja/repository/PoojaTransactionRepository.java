package com.rahul.kovil.pooja.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

	
	@Query("""
		    SELECT 
		        FUNCTION('MONTH', pt.createdAt),
		        COUNT(pt.id)
		    FROM PoojaTransaction pt
		    WHERE pt.createdAt BETWEEN :fromDate AND :toDate
		      AND pt.status = :status
		      AND pt.tenantId = :tenantId
		    GROUP BY FUNCTION('MONTH', pt.createdAt)
		    ORDER BY FUNCTION('MONTH', pt.createdAt)
		""")
		List<Object[]> findNoOfPoojaCompletedByMonth(
		        @Param("fromDate") LocalDateTime fromDate,
		        @Param("toDate") LocalDateTime toDate,
		        BaseStatus status, 
		        String tenantId
		);
		
		@Query("""
			    SELECT
			        CASE
			            WHEN FUNCTION('HOUR', pt.createdAt) BETWEEN 5 AND 11 THEN 'MORNING'
			            WHEN FUNCTION('HOUR', pt.createdAt) BETWEEN 12 AND 16 THEN 'AFTERNOON'
			            WHEN FUNCTION('HOUR', pt.createdAt) BETWEEN 17 AND 21 THEN 'EVENING'
			            ELSE 'OTHER'
			        END,
			        COUNT(pt.id)
			    FROM PoojaTransaction pt
			    WHERE pt.createdAt BETWEEN :startOfDay AND :endOfDay
			      AND pt.status = :status
			      AND pt.tenantId = :tenantId
			    GROUP BY
			        CASE
			            WHEN FUNCTION('HOUR', pt.createdAt) BETWEEN 5 AND 11 THEN 'MORNING'
			            WHEN FUNCTION('HOUR', pt.createdAt) BETWEEN 12 AND 16 THEN 'AFTERNOON'
			            WHEN FUNCTION('HOUR', pt.createdAt) BETWEEN 17 AND 21 THEN 'EVENING'
			            ELSE 'OTHER'
			        END
			""")
			List<Object[]> findTodayPoojaBySession(
			        @Param("startOfDay") LocalDateTime startOfDay,
			        @Param("endOfDay") LocalDateTime endOfDay,
			        @Param("status") BaseStatus status,
			        @Param("tenantId") String tenantId
			);


}
