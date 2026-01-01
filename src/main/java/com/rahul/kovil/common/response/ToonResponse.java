package com.rahul.kovil.common.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToonResponse {

	private String status;
	private String message;
	private List<String> label;
	private List<Object[]> data;

	public static ToonResponse error(String msg) {
		return ToonResponse.builder()
				.status("faild")
				.message(msg)
				.build();
	}
}
