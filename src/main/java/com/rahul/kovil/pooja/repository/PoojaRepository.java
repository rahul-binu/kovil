package com.rahul.kovil.pooja.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rahul.kovil.pooja.entity.Pooja;

@Repository
public interface PoojaRepository extends JpaRepository<Pooja, Long> {

	Pooja findByTransId(String transId);

}
