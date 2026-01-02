package com.rahul.kovil.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahul.kovil.inventory.entity.UnitConversion;

public interface UnitConversionRepository extends JpaRepository<UnitConversion, Long> {

	List<UnitConversion> findByTenantIdIn(List<String> of);

}
