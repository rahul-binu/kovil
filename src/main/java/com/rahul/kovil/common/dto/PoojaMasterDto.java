package com.rahul.kovil.common.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

import lombok.Data;

@Data
public class PoojaMasterDto {

    private Long id;

    private String prefix;
    
    private String name;

    private String groupName;

    private String description;

    private BigDecimal amount;
    private BigDecimal specialAmount;
    private boolean variableRate;

    private Integer durationInMinutes;
    private boolean onlyOnSpecificDays;

    private String allowedDays;

    private boolean requiresBookingDate;
    private LocalTime cutoffTime;

    private boolean requiresNakshathra;
    private boolean requiresGothra;

    private Integer tokenLimitPerDay;

    private String materialsList;

    private Integer displayOrder;
}
