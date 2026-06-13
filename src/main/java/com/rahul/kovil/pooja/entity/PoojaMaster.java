package com.rahul.kovil.pooja.entity;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.rahul.kovil.common.entity.BaseEntity;
import com.rahul.kovil.common.enums.BaseStatus;

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
@Table(name = "pooja_masters")
public class PoojaMaster extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 50)
	private String prefix;
	
	@Column(length = 50)
	private String name;
	
	@Column(length = 50)
	private String groupName = "";
	
	@Column(length = 255)
	private String description = "";
	
	private BigDecimal amount;
	private BigDecimal specialAmount;
	private boolean variableRate;
	
	private Integer durationInMinutes;
	private boolean onlyOnSpecificDays;
	@Column(length = 255)
	private String allowedDays = "";

	private boolean requiresBookingDate; // Is advance booking required?
	private LocalTime cutoffTime; // Last time in a day to allow booking

	private boolean requiresNakshathra;
	private boolean requiresGothra;

	private Integer tokenLimitPerDay;

	private String materialsList = "";

	private Integer displayOrder;
	
	@Enumerated(EnumType.STRING)
	private BaseStatus status = BaseStatus.ACTIVE;


	private long ledgerId;
}
