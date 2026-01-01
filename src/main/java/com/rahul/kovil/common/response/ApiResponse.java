package com.rahul.kovil.common.response;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
	private LocalDateTime timestamp = LocalDateTime.now();
	private HttpStatus status;
	private String message;
}
