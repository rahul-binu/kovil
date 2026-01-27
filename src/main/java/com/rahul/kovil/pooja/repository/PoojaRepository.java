package com.rahul.kovil.pooja.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.BookingStatus;
import com.rahul.kovil.pooja.entity.Pooja;

@Repository
public interface PoojaRepository extends JpaRepository<Pooja, Long> {

	Pooja findByTransId(String transId);

	@Modifying
	@Query("UPDATE Pooja p SET p.status = :status WHERE p.transId = :tid")
	void softDelete(String tid, BaseStatus status);

	@Query("""
			SELECT p FROM Pooja p WHERE p.status = :status AND p.bookingDate = :date AND p.tenantId = :tenantId AND p.bookingStatus = :bstatus
			""")
	List<Pooja> findPoojaBookingByDate(LocalDate date, BaseStatus status, String tenantId, BookingStatus bstatus);

}
