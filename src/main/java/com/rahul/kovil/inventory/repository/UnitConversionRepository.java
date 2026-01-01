package com.rahul.kovil.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahul.kovil.inventory.entity.UnitConversion;

public interface UnitConversionRepository extends JpaRepository<UnitConversion, Long> {

}
