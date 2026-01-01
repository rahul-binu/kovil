package com.rahul.kovil.user.entity;

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
@Table(name = "users")
public class User extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String transId;
	
	@Column(length = 60)
	private String userName;
	
	@Column(length = 75)
	private String password;
	
	@Column(length = 60)
	private String fullName;
	
	@Column(length = 100)
	private String email;
	
	@Column(length = 13)
	private String mobileNo;
	
	@Column(length = 255)
	private String address;
	
	@Column(length = 255)
	private String profilePicture;
	
	@Enumerated(EnumType.STRING)
	private BaseStatus status = BaseStatus.ACTIVE;

	@Override
	public String toString() {
		return "User [id=" + id + ", transId=" + transId + ", userName=" + userName + ", password=" + password
				+ ", fullName=" + fullName + ", email=" + email + ", mobileNo=" + mobileNo + ", address=" + address
				+ ", profilePicture=" + profilePicture + ", status=" + status + "]";
	}
	
	
	
}
