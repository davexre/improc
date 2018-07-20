package com.slavi.tools.dbcompare;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.slavi.dbutil.DbUtil;
import com.slavi.dbutil.ScriptRunner2;

public class DBCompare {
	static void copyDatabaseMetadata(Connection sourceConnToOracle, Connection sqlite, String tableNameSuffix) throws SQLException {
		try (Statement st = sqlite.createStatement()) {
			st.execute("drop table if exists " + "TABLES" + tableNameSuffix);
			st.execute("drop table if exists " + "TAB_COLUMNS" + tableNameSuffix);
			st.execute("drop table if exists " + "INDEXES" + tableNameSuffix);
			st.execute("drop table if exists " + "IND_COLUMNS" + tableNameSuffix);
			st.execute("drop table if exists " + "CONSTRAINTS" + tableNameSuffix);
			st.execute("drop table if exists " + "CONS_COLUMNS" + tableNameSuffix);
			st.execute("drop table if exists " + "VIEWS" + tableNameSuffix);
			st.execute("drop table if exists " + "MVIEWS" + tableNameSuffix);
			st.execute("drop table if exists " + "TRIGGERS" + tableNameSuffix);
			st.execute("drop table if exists " + "SOURCE" + tableNameSuffix);
		}

		try (Statement st = sourceConnToOracle.createStatement()) {
			int commitEveryNumRows = 10000;
			DbUtil.createResultSetSnapshot(st.executeQuery("select table_name, temporary, secondary, nested, compression, default_collation, external from user_tables"), sqlite, "TABLES" + tableNameSuffix, commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select table_name, column_name, data_type, data_length, data_precision, data_scale, nullable, column_id, collation from user_tab_columns"), sqlite, "TAB_COLUMNS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select index_name, index_type, table_owner, table_name, table_type, uniqueness, compression, temporary, generated, secondary from user_indexes"), sqlite, "INDEXES" + tableNameSuffix, commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select index_name, table_name, column_name, column_position, descend from user_ind_columns"), sqlite, "IND_COLUMNS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select owner, constraint_name, constraint_type, table_name, search_condition, r_owner, r_constraint_name, delete_rule, status from user_constraints"), sqlite, "CONSTRAINTS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select owner, constraint_name, table_name, column_name, position from user_cons_columns"), sqlite, "CONS_COLUMNS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select view_name, text_length, text_vc from user_views"), sqlite, "VIEWS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select owner, mview_name, container_name, query, query_len, updatable, update_log, refresh_mode, refresh_method, default_collation from user_mviews"), sqlite, "MVIEWS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select trigger_name, trigger_type, triggering_event, table_owner, base_object_type, table_name, column_name, when_clause, status, trigger_body, crossedition, before_statement, before_row, after_row, after_statement, instead_of_row, fire_once from user_triggers"), sqlite, "TRIGGERS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select name, type, line, text from user_source"), sqlite, "SOURCE" + tableNameSuffix, commitEveryNumRows);
		}
	}

	static boolean compare(Connection sqlite, StringBuilder report) throws Exception {
		ScriptRunner2 sr = new ScriptRunner2(sqlite);
		//sr.setLogWriter(null);
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
		return hasErrors;
	}
}
