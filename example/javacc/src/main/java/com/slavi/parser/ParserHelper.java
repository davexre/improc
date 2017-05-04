package com.slavi.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserHelper {
	Logger log = LoggerFactory.getLogger(getClass());

	public static class AliasItem {
		ManagedType type;
		String alias;
	}

	public final StringBuilder query = new StringBuilder();
	public final ArrayList paramVals = new ArrayList();

	EntityManager em;
	EntityType rootEntity;
	Map<String, AliasItem> aliases;
	public StringBuilder sql;
	String dateFormats[] = {
			"yyyy",
			"yyyy-MM",
			"yyyy-MM-dd",
			"yyyy-MM-dd'T'HH",
			"yyyy-MM-dd'T'HH:mm",
			"yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd'T'HH:mm:ss.SSS",
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
			"HH",
			"HH:mm",
			"HH:mm:ss",
			"HH:mm:ss.SSS",
			"HH:mm:ss.SSSZ",
	};

	String getName(String field) {
		int lastIndex = field.lastIndexOf(".");
		return lastIndex < 0 ? field : field.substring(lastIndex + 1);
	}

	String getParent(String field) {
		int lastIndex = field.lastIndexOf(".");
		return lastIndex < 0 ? "" : field.substring(0, lastIndex);
	}

	AliasItem getAlias(String field) {
		AliasItem r = aliases.get(field);
		if (r == null) {
			String fieldName = getName(field);
			AliasItem parentAlias = getAlias(getParent(field));
			String aliasName = "t" + aliases.size();
			r = new AliasItem();
			r.alias = aliasName + ".";
			Attribute attribute = parentAlias.type.getAttribute(fieldName);
			if (attribute.isCollection()) {
				PluralAttribute pa = (PluralAttribute) attribute;
				r.type = em.getMetamodel().entity(pa.getElementType().getJavaType());
			} else {
				r.type = em.getMetamodel().entity(attribute.getJavaType());
			}
			aliases.put(field, r);
			sql.append(" left join ").append(parentAlias.alias).append(fieldName).append(" ").append(aliasName);
		}
		return r;
	}

	public void addQueryTerm(String field, String operation, String fieldValue) throws ParseException {
		try {
			AliasItem parent = getAlias(getParent(field));
			field = getName(field);
			Class clazz = parent.type.getAttribute(field).getJavaType();
			String fieldExpr = parent.alias + field;
			Object o;
			if ("like".equals(operation)) {
				if (clazz == String.class || clazz == Character.class || clazz == char.class) {
					o = "%" + fieldValue + "%";
				} else if (clazz == Boolean.class || clazz == boolean.class) {
					o = Boolean.parseBoolean(fieldValue);
					operation = "=";
				} else if (clazz.isEnum()) {
					o = Enum.valueOf(clazz, fieldValue);
					operation = "=";
				} else {
					o = "%" + fieldValue + "%";
					fieldExpr = "CAST(" + fieldExpr + " AS CHAR(100))"; // TODO: This is hard-coded value - any "other" data type is converted to char(100)
				}
			} else {
				if (clazz == String.class || clazz == Character.class || clazz == char.class) {
					o = fieldValue;
				} else if (clazz == Boolean.class || clazz == boolean.class) {
					o = Boolean.parseBoolean(fieldValue);
				} else if (clazz == Byte.class || clazz == byte.class) {
					o = Byte.parseByte(fieldValue);
				} else if (clazz == Short.class || clazz == short.class) {
					o = Short.parseShort(fieldValue);
				} else if (clazz == Integer.class || clazz == int.class) {
					o = Integer.parseInt(fieldValue);
				} else if (clazz == Long.class || clazz == long.class) {
					o = Long.parseLong(fieldValue);
				} else if (clazz == Double.class || clazz == double.class) {
					o = Double.parseDouble(fieldValue);
				} else if (clazz == Float.class || clazz == float.class) {
					o = Float.parseFloat(fieldValue);
				} else if (clazz.isEnum()) {
					o = Enum.valueOf(clazz, fieldValue);
				} else if (
						clazz == java.sql.Date.class ||
						clazz == java.util.Date.class ||
						clazz == java.sql.Time.class ||
						clazz == java.sql.Timestamp.class
						) {
					o = DateUtils.parseDate(fieldValue, dateFormats);
				} else {
					o = fieldValue;
					fieldExpr = "CAST(" + fieldExpr + " AS CHAR(100))"; // TODO: This is hard-coded value - any "other" data type is converted to char(100)
				}
			}

			query.append(fieldExpr).append(" ").append(operation).append(" ?");
			paramVals.add(o);
		} catch (Throwable e) {
			log.error("Error parsing query", e);
			throw new ParseException("Error parsing field " + field + " " + operation + " " + fieldValue + ". Error message is " + e.getMessage());
		}
	}

	public ParserHelper(EntityManager em, EntityType rootEntity) {
		this.em = em;
		this.rootEntity = rootEntity;
		aliases = new HashMap<>();
		AliasItem root = new AliasItem();
		root.type = rootEntity;
		root.alias = "t0.";
		aliases.put("", root);
		sql = new StringBuilder("select t0 from ").append(rootEntity.getName()).append(" t0");
	}
}
