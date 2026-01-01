package com.rahul.kovil.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDto {
	private Long id;
	private String groupName;
	private Integer groupUnder;
	private String groupType;
	private String description;
	private Integer appLock;
	private String tenantId;
	private Integer orderNo;
}
