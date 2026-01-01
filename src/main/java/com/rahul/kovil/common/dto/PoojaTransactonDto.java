package com.rahul.kovil.common.dto;

import java.math.BigDecimal;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.pooja.entity.PoojaMaster;

import lombok.Data;

@Data
public class PoojaTransactonDto {
    private Long id;
    private String transId;
	private String prefix;
	private PoojaMaster poojaMaster;
    private BigDecimal amount;
    private BaseStatus status;
}
