package com.rahul.kovil.pooja.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.pooja.entity.Pooja;

@Repository
public interface PoojaRepository extends JpaRepository<Pooja, Long> {

	Pooja findByTransId(String transId);

	@Modifying
	@Query("UPDATE Pooja p SET p.status = :status WHERE p.transId = :tid")
	void softDelete(String tid, BaseStatus status);

}
