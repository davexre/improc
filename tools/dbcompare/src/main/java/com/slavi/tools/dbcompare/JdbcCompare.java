package com.slavi.tools.dbcompare;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slavi.dbutil.DbUtil;
import com.slavi.dbutil.ScriptRunner;
import com.slavi.util.Marker;

public class JdbcCompare {
	static final int commitEveryNumRows = 10000;

	static void copyRS(ResultSet rs, Connection targetConn, String targetTable, boolean createTargetTable) throws SQLException {
		if (createTargetTable) {
			DbUtil.createResultSetTable(rs, targetConn, targetTable);
		}
		DbUtil.copyResultSet(rs, targetConn, targetTable, commitEveryNumRows, false);
	}

	static void copyDatabaseMetadata(Connection sourceConnToOracle, Connection sqlite, String tableNameSuffix, String schemaToImport) throws SQLException {
		Logger log = LoggerFactory.getLogger(JdbcCompare.class);

		try (Statement st = sqlite.createStatement()) {
			st.execute("drop table if exists " + "TABLE_TYPES" + tableNameSuffix);
			st.execute("drop table if exists " + "TYPES" + tableNameSuffix);
			st.execute("drop table if exists " + "CATALOGS" + tableNameSuffix);
			st.execute("drop table if exists " + "SCHEMAS" + tableNameSuffix);
			st.execute("drop table if exists " + "TABLES" + tableNameSuffix);
			st.execute("drop table if exists " + "USER_TYPES" + tableNameSuffix);
			//st.execute("drop table if exists " + "SUPER_TABLES" + tableNameSuffix);
			//st.execute("drop table if exists " + "SUPER_TYPES" + tableNameSuffix);
			//st.execute("drop table if exists " + "ATTRIBUTES" + tableNameSuffix);
			st.execute("drop table if exists " + "CLIENT_INFO" + tableNameSuffix);
			st.execute("drop table if exists " + "TAB_COLUMNS" + tableNameSuffix);
			st.execute("drop table if exists " + "INDEXES" + tableNameSuffix);
			st.execute("drop table if exists " + "PRIMARY_KEYS" + tableNameSuffix);
			st.execute("drop table if exists " + "IMPORTED_KEYS" + tableNameSuffix);
			st.execute("drop table if exists " + "VERSION_COLS" + tableNameSuffix);
			st.execute("drop table if exists " + "BEST_ROW_ID" + tableNameSuffix);
			st.execute("drop table if exists " + "TABLE_PRIVS" + tableNameSuffix);
			st.execute("drop table if exists " + "COLUMN_PRIVS" + tableNameSuffix);
			st.execute("drop table if exists " + "PSEUDO_COLUMNS" + tableNameSuffix);
			st.execute("drop table if exists " + "PROCEDURES" + tableNameSuffix);
			st.execute("drop table if exists " + "PROC_COLUMNS" + tableNameSuffix);
			st.execute("drop table if exists " + "FUNCTIONS" + tableNameSuffix);
			st.execute("drop table if exists " + "FUNC_COLUMNS" + tableNameSuffix);

			DatabaseMetaData mdata = sourceConnToOracle.getMetaData();
			copyRS(mdata.getTableTypes(), sqlite, "TABLE_TYPES" + tableNameSuffix, true);
			copyRS(mdata.getTypeInfo(), sqlite, "TYPES" + tableNameSuffix, true);
			copyRS(mdata.getCatalogs(), sqlite, "CATALOGS" + tableNameSuffix, true);
			copyRS(mdata.getSchemas(null, schemaToImport), sqlite, "SCHEMAS" + tableNameSuffix, true);
			copyRS(mdata.getTables(null, schemaToImport, null, null), sqlite, "TABLES" + tableNameSuffix, true);
			copyRS(mdata.getUDTs(null, schemaToImport, null, null), sqlite, "USER_TYPES" + tableNameSuffix, true);
			//copyRS(mdata.getSuperTables(null, null, null), sqlite, "SUPER_TABLES" + tableNameSuffix, true);
			//copyRS(mdata.getSuperTypes(null, null, null), sqlite, "SUPER_TYPES" + tableNameSuffix, true);
			//copyRS(mdata.getAttributes(null, null, null, null), sqlite, "ATTRIBUTES" + tableNameSuffix, true);
			copyRS(mdata.getClientInfoProperties(), sqlite, "CLIENT_INFO" + tableNameSuffix, true);

			ResultSet rs = st.executeQuery("select * from TABLES" + tableNameSuffix + " where table_type in ('TABLE','GLOBAL TEMPORARY','LOCAL TEMPORARY','VIEW')");
			boolean first = true;
			while (rs.next()) {
				String catalog = rs.getString("TABLE_CAT");
				String schema = rs.getString("TABLE_SCHEM");
				String table = rs.getString("TABLE_NAME");
				// String tableType = rs.getString("TABLE_TYPE");
				log.debug("Copy Columns info for table {}", table);
				copyRS(mdata.getColumns(catalog, schema, table, null), sqlite, "TAB_COLUMNS" + tableNameSuffix, first);
				log.debug("Copy Indexes info for table {}", table);
				copyRS(mdata.getIndexInfo(catalog, schema, table, false, true), sqlite, "INDEXES" + tableNameSuffix, first);
				log.debug("Copy Primary key info for table {}", table);
				copyRS(mdata.getPrimaryKeys(catalog, schema, table), sqlite, "PRIMARY_KEYS" + tableNameSuffix, first);
				log.debug("Copy Imported keys info for table {}", table);
				copyRS(mdata.getImportedKeys(catalog, schema, table), sqlite, "IMPORTED_KEYS" + tableNameSuffix, first);
				log.debug("Copy Version columns info for table {}", table);
				copyRS(mdata.getVersionColumns(catalog, schema, table), sqlite, "VERSION_COLS" + tableNameSuffix, first);
				log.debug("Copy Best row identifier info for table {}", table);
				copyRS(mdata.getBestRowIdentifier(catalog, schema, table, 0, false), sqlite, "BEST_ROW_ID" + tableNameSuffix, first);
				log.debug("Copy Table privileges info for table {}", table);
				copyRS(mdata.getTablePrivileges(catalog, schema, table), sqlite, "TABLE_PRIVS" + tableNameSuffix, first);
				copyRS(mdata.getColumnPrivileges(catalog, schema, table, null), sqlite, "COLUMN_PRIVS" + tableNameSuffix, first);
				copyRS(mdata.getPseudoColumns(catalog, schema, table, null), sqlite, "PSEUDO_COLUMNS" + tableNameSuffix, first);

				// TODO: copyRS(mdata.getCrossReference(), sqlite, "CROSS_REF" + tableNameSuffix, first);
				first = false;
			}
			rs.close();

			copyRS(mdata.getProcedures(null, schemaToImport, null), sqlite, "PROCEDURES" + tableNameSuffix, true);
			rs = st.executeQuery("select * from PROCEDURES" + tableNameSuffix);
			first = true;
			while (rs.next()) {
				String catalog = rs.getString("PROCEDURE_CAT");
				String schema = rs.getString("PROCEDURE_SCHEM");
				String procedure = rs.getString("PROCEDURE_NAME");
				//String procedureType = rs.getString("PROCEDURE_TYPE");
				copyRS(mdata.getProcedureColumns(catalog, schema, procedure, null), sqlite, "PROC_COLUMNS" + tableNameSuffix, first);
				first = false;
			}
			rs.close();

			copyRS(mdata.getFunctions(null, schemaToImport, null), sqlite, "FUNCTIONS" + tableNameSuffix, true);
			rs = st.executeQuery("select * from FUNCTIONS" + tableNameSuffix);
			first = true;
			while (rs.next()) {
				String catalog = rs.getString("FUNCTION_CAT");
				String schema = rs.getString("FUNCTION_SCHEM");
				String function = rs.getString("FUNCTION_NAME");
				//String functionType = rs.getString("FUNCTION_TYPE");
				copyRS(mdata.getFunctionColumns(catalog, schema, function, null), sqlite, "FUNC_COLUMNS" + tableNameSuffix, first);
				first = false;
			}
			rs.close();
		}
	}

	static boolean compare(Connection sqlite, StringBuilder report) throws Exception {
		return false;
/*		ScriptRunner sr = new ScriptRunner(sqlite, true, true);
		sr.setLogWriter(null);
		sr.runScript(new InputStreamReader(Main.class.getResourceAsStream("DBCompare.sql")));

		List<AbstractMap.SimpleEntry<String, List<String>>> r = new ArrayList<>();
		String msg = null;
		List<String> lst = null;
		Statement st = sqlite.createStatement();
		ResultSet rs = st.executeQuery("select m.message, c.obj_name from compare c join compare_msg m on m.err_code = c.err_code order by 1,2");
		boolean hasErrors = false;
		while (rs.next()) {
			String tmp = StringUtils.trimToEmpty(rs.getString(1));
			if (!tmp.equals(msg)) {
				msg = tmp;
				lst = new ArrayList<>();
				r.add(new AbstractMap.SimpleEntry(msg, lst));
			}
			lst.add(StringUtils.trimToEmpty(rs.getString(2)));
			hasErrors = true;
		}

		StringWriter content = new StringWriter();
		Velocity.init();
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("errors", r);
		velocityContext.put("su", StringUtils.class);
		Velocity.evaluate(velocityContext, content, "", new InputStreamReader(Main.class.getResourceAsStream("DBCompare.vm")));
		report.append(content.toString());
		return hasErrors;*/
	}

	public static void main(String[] args) throws Exception {
		args = new String[] {
		"-jdbc",
		"-jdbcschema", "MMITEV",
/*
		"-f", "target/MyDbTest.sqlite",
		"-s", "(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
		"-su", "spetrov",
		"-sp", "spetrov",
*/
		"-t", "(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
		"-tu", "mmitev",
		"-tp", "mmitev",

		//"-c",
		//"asd?"
		};
		Marker.mark();
		System.exit(Main.main0(args));
		Marker.release();
		System.out.println("Done.");
	}
}
