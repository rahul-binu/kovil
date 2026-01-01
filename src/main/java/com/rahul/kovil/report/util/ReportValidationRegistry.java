package com.rahul.kovil.report.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ReportValidationRegistry {

    private final Map<String, Set<String>> allowedFields = new HashMap<>();

    public ReportValidationRegistry() {
        allowedFields.put("pt", Set.of("pt.createdAt", "pt.amount", "pt.poojaMaster"));
        allowedFields.put("pj", Set.of("pj.date", "pj.amount", "pj.devotee"));
        allowedFields.put("c", Set.of("c.name", "c.id"));
    }

    public boolean isValidField(String alias, String field) {
        return allowedFields.containsKey(alias)
               && allowedFields.get(alias).contains(field);
    }
}
