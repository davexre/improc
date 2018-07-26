package com.slavi.tools.dbexport;

import java.sql.ResultSet;

interface ExportResultSet {
	public void export(ResultSet rs, String fouName, String tableName) throws Exception;
}
