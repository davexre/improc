package com.slavi.dbutil;

import java.io.InputStream;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbDataParser {
	public List<DbDataValueParser> parsers;

	public PreparedStatement ps;

	int currentField = 0;

	public DbDataParser(PreparedStatement ps, DbDataParserTemplate defaults) throws SQLException {
		this.ps = ps;
		parsers = new ArrayList();
		ParameterMetaData md = ps.getParameterMetaData();
		for (int i = 1; i <= md.getParameterCount(); i++) {
			int mode = md.getParameterMode(i);
			DbDataValueParser p = null;
			if (mode == ParameterMetaData.parameterModeIn ||
				mode == ParameterMetaData.parameterModeInOut) {
				p = defaults.formatter.get(md.getParameterType(i));
			}
			parsers.add(p);
		}
	}

	public Object set(String value) throws Exception {
		if (currentField >= parsers.size())
			throw new Error("Field index out of range");
		DbDataValueParser p = parsers.get(currentField++);
		if (p == null)
			return null;
		Object o = p.parse(value);
		if (o instanceof InputStream)
			ps.setBlob(currentField, (InputStream) o);
		else
			ps.setObject(currentField, o);
		return o;
	}

	public void skip() {
		if (currentField >= parsers.size())
			throw new Error("Field index out of range");
		currentField++;
	}

	public int size() {
		return parsers.size();
	}

	public void reset() {
		currentField = 0;
	}
}
