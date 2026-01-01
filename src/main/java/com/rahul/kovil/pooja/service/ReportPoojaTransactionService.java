package com.rahul.kovil.pooja.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rahul.kovil.common.api.ReportServiceApi;
import com.rahul.kovil.common.response.ToonResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service("pt")
public class ReportPoojaTransactionService implements ReportServiceApi {

	@PersistenceContext
	private EntityManager em;

	public ToonResponse getReport(List<String> fields, Map<String, Object> filters, List<String> nfields) {

		String select = String.join(", ", fields);

		StringBuilder where = new StringBuilder();

		Map<String, Object> params = new HashMap<>();

		filters.forEach((f, v) -> {
			String param = f.replace(".", "_");
			if (where.length() > 0)
				where.append(" AND ");
			where.append(f).append(" = :").append(param);
			params.put(param, v);
		});

		String jpql = "SELECT " + select + " FROM PoojaTransaction pt";

		if (!where.isEmpty())
			jpql += " WHERE " + where;

		Query q = em.createQuery(jpql);

		params.forEach(q::setParameter);

		ToonResponse res = new ToonResponse();
		res.setStatus("SUCCESS");
		res.setData(q.getResultList());
		res.setLabel(nfields);
		return res;
	}

	public List<Object> extractLinkIds(){
		return null;
    }
}
