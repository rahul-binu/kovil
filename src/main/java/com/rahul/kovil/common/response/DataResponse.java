package com.rahul.kovil.common.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataResponse {
	private LocalDateTime timestamp;
	private String status;
	private Object data;
}
