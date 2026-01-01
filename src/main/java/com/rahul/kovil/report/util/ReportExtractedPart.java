package com.rahul.kovil.report.util;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ReportExtractedPart {

    private List<String> fields;              // safe validated fields
    private List<String> nameFields;
    private Map<String, Object> filters;      // safe validated filters
}