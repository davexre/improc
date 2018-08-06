package com.slavi.tools.dbcompare;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.slavi.dbutil.DbUtil;

public class OracleCompare {
	static void copyDatabaseMetadata(Connection sourceConnToOracle, Connection sqlite) throws SQLException {
		try (Statement st = sqlite.createStatement()) {
			st.execute("drop table if exists " + "TABLES");
			st.execute("drop table if exists " + "TAB_COLUMNS");
			st.execute("drop table if exists " + "INDEXES");
			st.execute("drop table if exists " + "IND_COLUMNS");
			st.execute("drop table if exists " + "CONSTRAINTS");
			st.execute("drop table if exists " + "CONS_COLUMNS");
			st.execute("drop table if exists " + "VIEWS");
			st.execute("drop table if exists " + "MVIEWS");
			st.execute("drop table if exists " + "TRIGGERS");
			st.execute("drop table if exists " + "SOURCE");
			st.execute("drop table if exists " + "TYPES");
			st.execute("drop table if exists " + "TYPE_ATTRS");
			st.execute("drop table if exists " + "TYPE_METHODS");
			st.execute("drop table if exists " + "TYPE_VERSIONS");
		}

		try (Statement st = sourceConnToOracle.createStatement()) {
			int commitEveryNumRows = 10000;
			DbUtil.createResultSetSnapshot(st.executeQuery("select table_name, temporary, secondary, nested, compression, default_collation, external from user_tables"), sqlite, "TABLES", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select table_name, column_name, data_type, data_length, data_precision, data_scale, nullable, column_id, collation from user_tab_columns"), sqlite, "TAB_COLUMNS", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select index_name, index_type, table_owner, table_name, table_type, uniqueness, compression, temporary, generated, secondary from user_indexes"), sqlite, "INDEXES", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select index_name, table_name, column_name, column_position, descend from user_ind_columns"), sqlite, "IND_COLUMNS", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select owner, constraint_name, constraint_type, table_name, search_condition, r_owner, r_constraint_name, delete_rule, status from user_constraints"), sqlite, "CONSTRAINTS", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select owner, constraint_name, table_name, column_name, position from user_cons_columns"), sqlite, "CONS_COLUMNS", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select view_name, text_length, text_vc from user_views"), sqlite, "VIEWS", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select owner, mview_name, container_name, query, query_len, updatable, update_log, refresh_mode, refresh_method, default_collation from user_mviews"), sqlite, "MVIEWS", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select trigger_name, trigger_type, triggering_event, table_owner, base_object_type, table_name, column_name, when_clause, status, trigger_body, crossedition, before_statement, before_row, after_row, after_statement, instead_of_row, fire_once from user_triggers"), sqlite, "TRIGGERS", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select name, type, line, text from user_source"), sqlite, "SOURCE", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select type_name, type_oid, typecode, attributes, methods, predefined, incomplete, final, instantiable from user_types"), sqlite, "TYPES", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select type_name, attr_name, attr_type_mod, attr_type_owner, attr_type_name, length, precision, scale, character_set_name, attr_no, inherited from user_type_attrs"), sqlite, "TYPE_ATTRS", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select type_name, method_name, method_no, method_type, parameters, results, final, instantiable, overriding, inherited from user_type_methods"), sqlite, "TYPE_METHODS", commitEveryNumRows);
			DbUtil.createResultSetSnapshot(st.executeQuery("select type_name, version#, typecode, status, line, text, hashcode from user_type_versions"), sqlite, "TYPE_VERSIONS", commitEveryNumRows);
		}
	}
}
