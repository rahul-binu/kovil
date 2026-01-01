package com.rahul.kovil.report.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.rahul.kovil.common.dto.DynamicReportRequest;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.BookingStatus;
import com.rahul.kovil.common.enums.VendorType;
import com.rahul.kovil.common.response.ToonResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class StaticReport {

	@PersistenceContext
	private EntityManager em;

	public Map<String, ToonResponse> getStaticPoojaReport(DynamicReportRequest req) {

		Map<String, ToonResponse> result = new HashMap<>();
		Map<String, List<String>> fields = req.getFields();
		Map<String, Map<String, Object>> filters = req.getFilters();
		Map<String, List<String>> nameFields = req.getNameFields();

		List<String> poojaTransIds = new ArrayList<>();
		List<String> devoteeTransIds = new ArrayList<>();

		if (fields.containsKey("pj")) {

			String select = String.join(", ", fields.get("pj"));
			StringBuilder where = new StringBuilder();
			Map<String, Object> params = new HashMap<>();
			AtomicInteger counter = new AtomicInteger();

			if (filters.containsKey("pj")) {
				filters.get("pj").forEach((key, value) -> {

					String base = key.replaceAll("[^A-Za-z0-9]", "_");
					String param = base + "_" + counter.getAndIncrement(); // UNIQUE

					if (where.length() > 0)
						where.append(" AND ");

					where.append(key).append(" :").append(param);
					params.put(param, convertValue(key, value));
				});
			}

			String jpql = "SELECT " + select + " FROM Pooja pj WHERE pj.status = :status";
			if (!where.isEmpty())
				jpql += " AND " + where;

			Query q = em.createQuery(jpql);
			params.forEach(q::setParameter);
			q.setParameter("status", BaseStatus.ACTIVE);

			List<Object[]> rows = q.getResultList();

			// Extract transIds
			poojaTransIds = rows.stream()
					.map(row -> row != null && row.length > 2 && row[2] != null ? row[2].toString() : null)
					.filter(Objects::nonNull).toList();

			// Extract devoteeIds
			devoteeTransIds = rows.stream()
					.map(row -> row != null && row.length > 0 && row[0] != null ? row[0].toString() : null)
					.filter(Objects::nonNull).toList();

			result.put("pj", ToonResponse.builder().status("success").label(nameFields.get("pj")).data(rows)
					.message("pooja master fetched").build());
		}

		if (fields.containsKey("cs") && !devoteeTransIds.isEmpty()) {

			String select = String.join(", ", fields.get("cs"));
			StringBuilder where = new StringBuilder();
			Map<String, Object> params = new HashMap<>();
			AtomicInteger counter = new AtomicInteger();

			if (filters.containsKey("cs")) {
				filters.get("cs").forEach((key, value) -> {

					String base = key.replaceAll("[^A-Za-z0-9]", "_");
					String param = base + "_" + counter.getAndIncrement();

					if (where.length() > 0)
						where.append(" AND ");

					where.append(key).append(" :").append(param);
					params.put(param, value);
				});
			}
			String jpql = "SELECT " + select + " FROM Vendor cs WHERE cs.transId IN :devotee";
			if (!where.isEmpty())
				jpql += " AND " + where;

			Query q = em.createQuery(jpql);
			params.forEach(q::setParameter);
			q.setParameter("devotee", devoteeTransIds);

			List<Object[]> rows = q.getResultList();

			result.put("cs", ToonResponse.builder().status("success").label(nameFields.get("cs")).data(rows)
					.message("customers fetched").build());
		}

		if (fields.containsKey("pt") && !poojaTransIds.isEmpty()) {

			String select = String.join(", ", fields.get("pt"));
			StringBuilder where = new StringBuilder();
			Map<String, Object> params = new HashMap<>();
			AtomicInteger counter = new AtomicInteger();

			if (filters.containsKey("pt")) {
				filters.get("pt").forEach((key, value) -> {

					String base = key.replaceAll("[^A-Za-z0-9]", "_");
					String param = base + "_" + counter.getAndIncrement();

					if (where.length() > 0)
						where.append(" AND ");

					where.append(key).append(" :").append(param);
					params.put(param, value);
				});
			}
			select = select.replace("pt.receiptNo", "function('replace', pt.prefix, '@N@', CONCAT('', pt.receiptNo))");
			String jpql = "SELECT " + select + " FROM PoojaTransaction pt WHERE pt.transId IN :transIds";
			if (!where.isEmpty())
				jpql += " AND " + where;

			Query q = em.createQuery(jpql);
			params.forEach(q::setParameter);
			q.setParameter("transIds", poojaTransIds);

			List<Object[]> rows = q.getResultList();

			result.put("pt", ToonResponse.builder().status("success").label(nameFields.get("pt")).data(rows)
					.message("pooja transactions fetched").build());
		}

		return result;
	}

	private Object convertValue(String key, Object value) {

		if (value instanceof String s) {

			if (key.contains("bookingStatus")) {
				return BookingStatus.valueOf(value.toString());
			}

			// detect date (yyyy-MM-dd)
			if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {

				LocalDate d = LocalDate.parse(s);

				if (key.contains(">=")) {
					return d.atStartOfDay(); // 00:00:00
				}

				if (key.contains("<=")) {
					return d.atTime(23, 59, 59, 999999999); // end of day
				}

				// default convert to start-of-day LocalDateTime
				return d.atStartOfDay();
			}

			return s;
		}

		return value;
	}

	public Map<String, ToonResponse> getStaticVendorReport(String vtype, String from, String to) {
		Map<String, ToonResponse> res = new HashMap<>();
		
		String jpql = "SELECT v.id, v.transId, v.fullName, v.mobile, v.familyName, v.address, v.nakshathra, v.createdAt FROM Vendor v WHERE v.type = :vtype AND v.createdAt BETWEEN :from AND :to";
		Query query = em.createQuery(jpql);
		query.setParameter("vtype", VendorType.valueOf(vtype.toUpperCase()));
		query.setParameter("from", LocalDate.parse(from).atStartOfDay());
		query.setParameter("to", LocalDate.parse(to).atTime(LocalTime.MAX));
		
		List<Object[]> vr = query.getResultList();
		List<String> vl = List.of("id", "tid", "vn", "mob" ,"fnm", "add", "nak", "cdt");
		
		ToonResponse venodr = ToonResponse.builder().label(vl).data(vr).status("success").build();
		
		res.put("vendor", venodr);
		return res;
	}
}
