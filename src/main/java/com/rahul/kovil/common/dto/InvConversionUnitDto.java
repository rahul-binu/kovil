package com.rahul.kovil.common.dto;

import lombok.Data;

@Data
public class InvConversionUnitDto {
	private String fromUnit;
	private String toUnit;
	private Double multiplier;
}
