package com.rahul.kovil.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class SiteHelper {

	public SiteHelper() {
//        System.out.println(transId("transaction"));
	}

	/**
	 * @return unique key containing timestamp and UUID
	 */
	public static String transId(String prefix) {
		prefix = (prefix == null || prefix.isBlank()) ? "txn" : prefix.substring(0, Math.min(3, prefix.length()));
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
		String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
		return prefix + "." + timestamp + "." + uuid;
	}

	/**
	 * Converts String → LocalDateTime
	 *
	 * @param endOfDay if true and only date is provided, returns 23:59:59.999999999
	 */
	public static LocalDateTime toLocalDateTime(String dateStr, boolean endOfDay) {
		final List<DateTimeFormatter> formatters = List.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME, // 2025-01-01T10:30:45
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
				DateTimeFormatter.ISO_LOCAL_DATE // 2025-01-01
		);
		if (dateStr == null || dateStr.isBlank())
			throw new IllegalArgumentException("Date string is null or empty");

		for (DateTimeFormatter formatter : formatters) {
			try {
				if (formatter == DateTimeFormatter.ISO_LOCAL_DATE) {
					LocalDate date = LocalDate.parse(dateStr, formatter);
					return endOfDay ? date.atTime(LocalTime.MAX) : date.atStartOfDay();
				}
				return LocalDateTime.parse(dateStr, formatter);
			} catch (DateTimeParseException ignored) {
			}
		}

		throw new IllegalArgumentException("Unsupported date format: " + dateStr);
	}

	/**
	 * Converts String → LocalDate
	 */
	public static LocalDate toLocalDate(String dateStr) {
		if (dateStr == null || dateStr.isBlank())
			throw new IllegalArgumentException("Date string is null or empty");

		// Try LocalDate directly
		try {
			return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
		} catch (DateTimeParseException ignored) {
		}

		// Try LocalDateTime then extract date
		LocalDateTime ldt = toLocalDateTime(dateStr, false);
		return ldt.toLocalDate();
	}

}