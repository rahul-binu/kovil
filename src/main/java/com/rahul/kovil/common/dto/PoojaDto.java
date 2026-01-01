package com.rahul.kovil.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.rahul.kovil.common.enums.BaseStatus;

import lombok.Data;

@Data
public class PoojaDto {

    private Long id;
    private String devotee;
    private String user;
    private String transId;
    private LocalDateTime date;
    private BigDecimal amount;
    private BaseStatus status;

    public void setDate(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        LocalTime now = LocalTime.now();
        this.date = LocalDateTime.of(date, now);
    }
}
