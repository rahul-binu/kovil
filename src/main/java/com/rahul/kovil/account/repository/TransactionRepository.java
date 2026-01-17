package com.rahul.kovil.account.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rahul.kovil.account.entity.Transaction;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.TransactionStatus;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	@Query("""
			SELECT MAX(t.voucherNo) FROM Transaction t
			      WHERE t.voucherType = :voucherType
			        AND t.tenantId = :tenantId
			        AND t.status = :status
			        """)
	Long findMaxVoucherNumberByVoucherTypeAndTenantIdAndStatus(String voucherType, String tenantId,
			TransactionStatus status);

	@Query("""
			    SELECT
			        t.createdUser, t.voucherNo, t.voucherType, t.creditLedger, t.debitLedger, t.amount, t.transactionDate, t.remark, t.type, t.referenceNo, t.referenceDate, t.transId
			    FROM Transaction t
			    WHERE t.tenantId = :tenantId
			      AND t.status IN :statusList
			      AND t.transactionDate BETWEEN :from AND :to
			      AND t.voucherType IN :voucherType
			""")
	List<Object[]> findByTenantIdAndStatusInAndTransactionDateBetweenAndVoucherTypeIn(
			@Param("tenantId") String tenantId, @Param("statusList") List<TransactionStatus> statusList,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("voucherType") List<String> voucherType);

	@Query("""
			    SELECT
			        t.createdUser, t.voucherNo, t.voucherType, t.creditLedger, t.debitLedger, t.amount, t.transactionDate, t.remark, t.type, t.referenceNo, t.referenceDate, t.transId
			    FROM Transaction t
			    WHERE t.tenantId = :tenantId
			      AND t.status IN :statusList
			      AND t.transactionDate BETWEEN :from AND :to
			""")
	List<Object[]> findByTenantIdAndStatusInAndTransactionDateBetween(@Param("tenantId") String tenantId,
			@Param("statusList") List<TransactionStatus> statusList, @Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);

	@Query(value = "(SELECT -SUM(ac1.amount) AS a, ac1.debit AS acc FROM accounts_book ac1 "
			+ "WHERE ac1.branch_id IN (:branchId) " + "AND DATE(ac1.trans_date) < :onDate "
			+ "AND DATE(ac1.trans_date) >= :openDate " + "AND ac1.debit IN (:ledgers) "
			+ "AND ac1.status IN ('', 'active') " + "GROUP BY ac1.debit) " + "UNION ALL "
			+ "(SELECT SUM(ac2.amount) AS a, ac2.credit AS acc FROM accounts_book ac2 "
			+ "WHERE ac2.branch_id IN (:branchId) " + "AND DATE(ac2.trans_date) < :onDate "
			+ "AND DATE(ac2.trans_date) >= :openDate " + "AND ac2.credit IN (:ledgers) "
			+ "AND ac2.status IN ('', 'active') " + "GROUP BY ac2.credit)", nativeQuery = true)
	List<Object[]> getBalanceOfLedgers(@Param("branchId") List<Integer> branchIds, @Param("openDate") String openDate,
			@Param("onDate") String onDate, @Param("ledgers") List<Integer> ledgers);

	
	@Query("""
			SELECT t1.creditLedger AS led, -SUM(t1.amount) AS amt FROM Transaction t1
				WHERE t1.tenantId IN :tenantIds 
					AND t1.transactionDate < :from
					AND t1.transactionDate >= :openDate
					AND t1.status IN :status
					AND t1.creditLedger IN :ledgers
				GROUP BY t1.creditLedger
			UNION ALL
			SELECT t1.debitLedger AS led, SUM(t1.amount) AS amt FROM Transaction t1
				WHERE t1.tenantId IN :tenantIds 
					AND t1.transactionDate < :from
					AND t1.transactionDate >= :openDate
					AND t1.status IN :status
					AND t1.debitLedger IN :ledgers
				GROUP BY t1.debitLedger
			
			""")
	List<Object[]> currentLedgerBalance(List<Long> ledgers ,List<TransactionStatus> status, List<String> tenantIds, LocalDateTime openDate,
			LocalDateTime from);

	Transaction findByTransId(String id);
	
	@Modifying
	@Query("UPDATE Transaction t SET t.status = :status WHERE t.transId = :tid")
	void softDelete(String tid, TransactionStatus status);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
