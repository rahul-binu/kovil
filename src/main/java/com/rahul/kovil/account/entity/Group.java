package com.rahul.kovil.account.entity;

import com.rahul.kovil.common.enums.BaseStatus;
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
@Table(name = "acc_groups")
public class Group extends BaseEntity{
		
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String createdUser;
	
	@Column(length = 60)
	private String groupName;
	
	private Integer groupUnder;
	
	private String groupType;
	
	@Column(length = 100)
	private String description;
	
	@Column(name = "app_lock", length = 3)
	Integer appLock;
	
	@Column(length = 3)
	Integer orderNo;
	
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BaseStatus status = BaseStatus.ACTIVE;
}
