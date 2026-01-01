package com.rahul.kovil.inventory.entity;

import com.rahul.kovil.common.entity.BaseEntity;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.ItemUnitType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "inv_items")
public class Item extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String itemCode="";
	
	@Column(nullable = false)
	private String itemGroup="";

	@Column(nullable = false)
	private String itemName="";
	
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemUnitType baseUnit;

    private String description;
    
    @Enumerated(EnumType.STRING)
    private BaseStatus status = BaseStatus.ACTIVE;
}
