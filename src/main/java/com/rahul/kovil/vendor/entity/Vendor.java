package com.rahul.kovil.vendor.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.Gender;
import com.rahul.kovil.common.enums.Nakshathra;
import com.rahul.kovil.common.enums.VendorType;
import com.rahul.kovil.common.entity.BaseEntity;

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
@Table(name = "vendors")
public class Vendor extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	private Long accountId;
	
	@Column(length = 60)
	private String transId;
	
	@Column(length = 50)
	private String fullName;
	
	@Enumerated(EnumType.STRING)
	private VendorType type;
	
	@Enumerated(EnumType.STRING)
	private Gender gender;
	
	// contact details
	@Column(length = 13)
	private String mobile;
	
	@Column(length = 100)
	private String email;

	@Column(length = 150)
	private String familyName;
		
	@Column(length = 255)
	private String address;
	
	@Column(length = 15)
	private Long pincode;
	
	@Column(length = 40)
	private String state;
	
	// bank details
	@Column(length = 100)
	private String bankName;

	@Column(length = 100)
	private String bankBranchName;
	
	@Column(length = 30)
	private String bankAccNo;
	
	@Column(length = 30)
	private String bankIfsc;
	
	// shop details
	@Column(length = 80)
	private String shopName;
	
	@Column(length = 100)
	private String gstNo;
	
	
	@Column(length = 255)
	private String photo;
	
	@Enumerated(EnumType.STRING)
	private BaseStatus status = BaseStatus.ACTIVE;
	
	// devotee details
	@Enumerated(EnumType.STRING)
	private Nakshathra nakshathra;
	
	private LocalDate dateOfBirth;
	
	private LocalTime timeOfBirth;
	
	@Column(length = 100)
	private String placeOfBirth;
	
	// other details
	@Column(length = 255)
	private String 	remarks;
	
}
