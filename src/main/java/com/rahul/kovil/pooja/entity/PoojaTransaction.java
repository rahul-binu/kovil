package com.rahul.kovil.pooja.entity;

import java.math.BigDecimal;

import com.rahul.kovil.common.entity.BaseEntity;
import com.rahul.kovil.common.enums.BaseStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pooja_transactions")
public class PoojaTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 60)
    private String transId;
    
    @Column(length = 60)
    private String vendorId;
        
    @ManyToOne
    @JoinColumn(name = "pooja_master_id", nullable = false)
    private PoojaMaster poojaMaster;

    @Column(length = 50)
    private String prefix;
    
    private Long receiptNo;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    

    @Enumerated(EnumType.STRING)
    private BaseStatus status = BaseStatus.ACTIVE;
    
}
