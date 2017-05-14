package com.slavi.parser;

import java.io.StringReader;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.StringUtils;

public class WebFilterParser<T> {

	StringBuilder where;

	WebFilterParserState helper;

	Class<T> rootClass;

	public WebFilterParser(EntityManager em, Class<T> rootClass) {
		this.rootClass = rootClass;
		EntityType rootEntity = em.getMetamodel().entity(rootClass);
		helper = new WebFilterParserState(em, rootEntity);
		where = new StringBuilder();
	}

	public void addQuery(String query, List bindVariables) throws ParseException {
		if (StringUtils.isEmpty(query))
			return;
		helper.externalBindVariables = bindVariables;
		helper.nextExternalBindVariable = 0;
		MyParser parser = new MyParser(new StringReader(query));
		parser.helper = helper;
		parser.parse();
		if (helper.externalBindVariables != null &&
			helper.externalBindVariables.size() != helper.nextExternalBindVariable) {
			throw new ParseException("Query specifies less bind variables than supplied.");
		}
		if (helper.query.length() > 0) {
			if (where.length() > 0)
				where.append(" and ");
			where.append("(").append(helper.query).append(")");
		}
	}

	public List<T> execute(Filter paging) throws ParseException {
		StringBuilder q = new StringBuilder();
		q.append(helper.sql);
		if (where.length() > 0)
			q.append(" where ");
		q.append(where).append(" order by ").append(helper.buildOrderBy(paging.sort));
		System.out.println("\n\n\n" + q);
		TypedQuery<T> query = helper.em.createQuery(q.toString(), rootClass);
		for (int i = 0; i < helper.paramVals.size(); i++)
			query.setParameter(i + 1, helper.paramVals.get(i));
		if (paging != null) {
			query.setFirstResult((paging.getPage() - 1) * paging.getSize());
			query.setMaxResults(paging.getSize());
		}
		return query.getResultList();
	}

	public List<T> detachAll(List<T> items) {
		if (items != null)
			for (T item : items)
				helper.em.detach(item);
		return items;
	}
}
