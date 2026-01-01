package com.rahul.kovil.inventory.entity;

import com.rahul.kovil.common.entity.BaseEntity;
import com.rahul.kovil.common.enums.ItemUnitType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "inv_unit_conversions", uniqueConstraints = @UniqueConstraint(columnNames = { "fromUnit", "toUnit" }))
public class UnitConversion extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ItemUnitType fromUnit;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ItemUnitType toUnit;

	@Column(nullable = false)
	private Double multiplier=0.0;
}
