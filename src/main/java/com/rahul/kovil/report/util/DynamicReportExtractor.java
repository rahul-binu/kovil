package com.rahul.kovil.report.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.rahul.kovil.common.dto.DynamicReportRequest;

import groovyjarjarantlr4.v4.parse.ANTLRParser.sync_return;

@Component
public class DynamicReportExtractor {

    private final ReportValidationRegistry registry;

    public DynamicReportExtractor(ReportValidationRegistry registry) {
        this.registry = registry;
    }

    public Map<String, ReportExtractedPart> extract(DynamicReportRequest req) {

        Map<String, ReportExtractedPart> out = new HashMap<>();

        for (String alias : req.getFields().keySet()) {

            ReportExtractedPart part = new ReportExtractedPart();

            // ----------------------
            // VALIDATE & EXTRACT FIELDS
            // ----------------------
            List<String> validFields = req.getFields()
                    .get(alias)
                    .stream()
                    .filter(f -> registry.isValidField(alias, f))
                    .collect(Collectors.toList());

            if (validFields.isEmpty()) continue;

            part.setFields(validFields);
            
            List<String> validNameFields = req.getNameFields()
                    .get(alias)
                    .stream()
                    .collect(Collectors.toList());


            part.setNameFields(validNameFields);

            // ----------------------
            // VALIDATE & EXTRACT FILTERS
            // ----------------------
            Map<String, Object> filters = new HashMap<>();

            if (req.getFilters() != null && req.getFilters().containsKey(alias)) {

                for (Map.Entry<String, Object> e : req.getFilters().get(alias).entrySet()) {

                    if (!registry.isValidField(alias, e.getKey())) continue;

                    filters.put(e.getKey(), e.getValue());
                }
            }

            part.setFilters(filters);
            out.put(alias, part);
        }

        return out;
    }
}
