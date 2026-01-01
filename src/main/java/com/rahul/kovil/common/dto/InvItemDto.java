package com.rahul.kovil.common.dto;

import com.rahul.kovil.common.enums.ItemUnitType;

import lombok.Data;

@Data
public class InvItemDto {
	private Long id;
	private String itemCode;
	private String itemGroup;
	private String itemName;
    private ItemUnitType baseUnit;
    private String description;
}
