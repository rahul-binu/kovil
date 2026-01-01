package com.rahul.kovil.common.api;

import java.util.List;
import java.util.Map;

import com.rahul.kovil.common.response.ToonResponse;

public interface ReportServiceApi {
	ToonResponse getReport(List<String> fields, Map<String, Object> filters, List<String> nfields);

	List<Object> extractLinkIds();
}
