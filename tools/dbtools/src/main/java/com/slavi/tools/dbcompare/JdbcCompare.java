package com.slavi.tools.dbcompare;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slavi.dbutil.DbUtil;

public class JdbcCompare {
	public static final int commitEveryNumRows = 10000;

	static void copyRS(ResultSet rs, Connection targetConn, String targetTable, boolean createTargetTable) throws SQLException {
		if (createTargetTable) {
			DbUtil.createResultSetTable(rs, targetConn, targetTable);
		}
		DbUtil.copyResultSet(rs, targetConn, targetTable, commitEveryNumRows, false);
	}

	static void copyDatabaseMetadata(Connection sourceConnToOracle, Connection sqlite, String schemaToImport) throws SQLException {
		Logger log = LoggerFactory.getLogger(JdbcCompare.class);

		try (Statement st = sqlite.createStatement()) {
			st.execute("drop table if exists TABLES");
			st.execute("drop table if exists USER_TYPES");
			st.execute("drop table if exists TAB_COLUMNS");
			st.execute("drop table if exists INDEXES");
			st.execute("drop table if exists PRIMARY_KEYS");
			st.execute("drop table if exists IMPORTED_KEYS");
			st.execute("drop table if exists PROCEDURES");
			st.execute("drop table if exists PROC_COLUMNS");
			st.execute("drop table if exists FUNCTIONS");
			st.execute("drop table if exists FUNC_COLUMNS");

			DatabaseMetaData mdata = sourceConnToOracle.getMetaData();
			copyRS(mdata.getTables(null, schemaToImport, null, null), sqlite, "TABLES", true);
			copyRS(mdata.getUDTs(null, schemaToImport, null, null), sqlite, "USER_TYPES", true);

			ResultSet rs = st.executeQuery("select * from TABLES where table_type in ('TABLE','GLOBAL TEMPORARY','LOCAL TEMPORARY','VIEW') order by table_name");
			boolean first = true;
			while (rs.next()) {
				String catalog = rs.getString("TABLE_CAT");
				String schema = rs.getString("TABLE_SCHEM");
				String table = rs.getString("TABLE_NAME");
				// String tableType = rs.getString("TABLE_TYPE");
				log.debug("Copy Columns info for table {}", table);
				copyRS(mdata.getColumns(catalog, schema, table, null), sqlite, "TAB_COLUMNS", first);
				log.debug("Copy Indexes info for table {}", table);
				copyRS(mdata.getIndexInfo(catalog, schema, table, false, true), sqlite, "INDEXES", first);
				log.debug("Copy Primary key info for table {}", table);
				copyRS(mdata.getPrimaryKeys(catalog, schema, table), sqlite, "PRIMARY_KEYS", first);
				log.debug("Copy Imported keys info for table {}", table);
				copyRS(mdata.getImportedKeys(catalog, schema, table), sqlite, "IMPORTED_KEYS", first);

				first = false;
			}
			rs.close();

			copyRS(mdata.getProcedures(null, schemaToImport, null), sqlite, "PROCEDURES", true);
			rs = st.executeQuery("select * from PROCEDURES order by procedure_name");
			first = true;
			while (rs.next()) {
				String catalog = rs.getString("PROCEDURE_CAT");
				String schema = rs.getString("PROCEDURE_SCHEM");
				String procedure = rs.getString("PROCEDURE_NAME");
				//String procedureType = rs.getString("PROCEDURE_TYPE");
				copyRS(mdata.getProcedureColumns(catalog, schema, procedure, null), sqlite, "PROC_COLUMNS", first);
				first = false;
			}
			rs.close();

			copyRS(mdata.getFunctions(null, schemaToImport, null), sqlite, "FUNCTIONS", true);
			rs = st.executeQuery("select * from FUNCTIONS order by function_name");
			first = true;
			while (rs.next()) {
				String catalog = rs.getString("FUNCTION_CAT");
				String schema = rs.getString("FUNCTION_SCHEM");
				String function = rs.getString("FUNCTION_NAME");
				//String functionType = rs.getString("FUNCTION_TYPE");
				copyRS(mdata.getFunctionColumns(catalog, schema, function, null), sqlite, "FUNC_COLUMNS", first);
				first = false;
			}
			rs.close();
		}
	}
}
