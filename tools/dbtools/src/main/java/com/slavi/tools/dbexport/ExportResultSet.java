package com.slavi.tools.dbexport;

import java.sql.ResultSet;

interface ExportResultSet extends AutoCloseable {
	public void export(ResultSet rs, String fouName, String tableName) throws Exception;

	public default void close() {};
}
