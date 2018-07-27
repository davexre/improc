package com.slavi.tools.dbexport;

import java.io.File;
import java.io.PrintWriter;
import java.sql.ResultSet;

import org.apache.commons.csv.CSVFormat;

class CSVExport implements ExportResultSet {
	CSVFormat csv;
	public CSVExport(CSVFormat csv) {
		this.csv = csv;
	}

	public void export(ResultSet rs, String fouName, String tableName) throws Exception {
		try (PrintWriter out = new PrintWriter(new File(fouName))) {
			csv.withHeader(rs).print(out).printRecords(rs);
		}
	}
}