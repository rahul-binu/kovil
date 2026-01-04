package com.rahul.kovil.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.rahul.kovil.common.enums.TransactionStatus;
import com.rahul.kovil.common.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private String createdUser;
    private String voucherType;
    private Long voucherNo;
    private Long creditLedger;  
    private Long debitLedger;   
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String remark;

    private TransactionType type;
    private TransactionStatus status = TransactionStatus.ACTIVE;
    private String transId;
    private LocalDate referenceDate;
    private String referenceNo;
}
