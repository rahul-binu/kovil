package com.rahul.kovil.inventory.entity;

import java.time.LocalDateTime;

import com.rahul.kovil.common.entity.BaseEntity;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.ItemTransactionType;
import com.rahul.kovil.common.enums.ItemUnitType;
import com.rahul.kovil.common.enums.StockDirection;

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
@Setter
@Getter
@Table(name = "inv_stock_transactions")
public class StockTransaction extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockDirection stockDirection; // IN / OUT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemTransactionType transactionType; // SALE / PURCHASE / CONVERSION
    
    
    @Column(nullable = false)
    private Double quantity; // in transaction unit

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemUnitType transactionUnit; // NOS, KG, G, L, ML, DOZEN, PACK, BOX

    @Column(nullable = false)
    private Double unitMultiplier; // multiplier to convert transactionUnit -> item.baseUnit

    private String remarks;

    private LocalDateTime transactionDate = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BaseStatus status = BaseStatus.ACTIVE;

	@Override
	public String toString() {
		return "StockTransaction [id=" + id + ", item=" + item + ", stockDirection=" + stockDirection
				+ ", transactionType=" + transactionType + ", quantity=" + quantity + ", transactionUnit="
				+ transactionUnit + ", unitMultiplier=" + unitMultiplier + ", remarks=" + remarks + ", transactionDate="
				+ transactionDate + ", status=" + status + "]";
	} 
    
    
}
