package com.rahul.kovil.account.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.rahul.kovil.common.enums.TransactionStatus;
import com.rahul.kovil.common.enums.TransactionType;
import com.rahul.kovil.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "acc_transactions")
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String createdUser;

    @Column(length = 50, nullable = false)
    private String voucherType;
   
    @Column(nullable = false)
    private Long voucherNo;

    @Column(nullable = false)
    private Long creditLedger;  // From side

    @Column(nullable = false)
    private Long debitLedger;    // To side

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(length = 60)
    private String referenceNo="";
    
    @Column(columnDefinition = "DATE")
    private LocalDate referenceDate;
    
    @Column(length = 150)
    private String remark;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.ACTIVE;
    
    @Column(length = 60, nullable = false)
    private String transId;
}
