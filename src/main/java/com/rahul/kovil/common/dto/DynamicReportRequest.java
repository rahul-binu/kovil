package com.rahul.kovil.common.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DynamicReportRequest {
    private Map<String, List<String>> fields;      // group → list of fields
    private Map<String, Map<String, Object>> filters; // group → field → value
    private Map<String, List<String>> nameFields;
}
