package com.slavi.tools.dbexport.test;

import com.slavi.tools.dbexport.Main;

public class TestDbExport {
	static String[] exportTables = new String[] {
			"-c",
			"-t", "aa_tmp_change_first_name, user_tables, not_existing_table",
			"-f", "target/output-%t.csv",
			"-url", "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
			"-u", "spetrov",
			"-p", "spetrov"
		};

	static String[] exportTablesSql = new String[] {
			"-c",
			"-mode", "sql",
			"-t", "aa_tmp_change_first_name, user_tables",
			"-f", "target/%t.sql",
			"-url", "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
			"-u", "spetrov",
			"-p", "spetrov"
		};

	static String[] exportTablesTargetDb = new String[] {
			"-mode", "targetDb",
			"-t", "aa_tmp_change_first_name, user_tables",
			"-url", "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
			"-u", "spetrov",
			"-p", "spetrov",
			"-turl", "jdbc:sqlite:target/targetDb.sqlite"
		};

	static String[] exportSqlTargetDbSQLite = new String[] {
			"-mode", "targetDb",
			"-sql", "select * from user_tab_columns",
			"-url", "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
			"-u", "spetrov",
			"-p", "spetrov",
			"-turl", "jdbc:sqlite:target/targetDb.sqlite",
			"-et", "append"
		};

	static String[] exportSqlTargetDbHSQL = new String[] {
			"-mode", "targetDb",
			"-sql", "select sysdate, current_date, sys_context('userenv','current_schema') from dual;",
			"-url", "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
			"-u", "spetrov",
			"-p", "spetrov",
			"-turl", "jdbc:hsqldb:file:target/targetDb.hsqldb",
			"-et", "append"
		};

	static String[] exportSqlTargetDbH2 = new String[] {
			"-mode", "targetDb",
			"-sql", "select sysdate, current_date, sys_context('userenv','current_schema') from dual;",
			"-url", "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
			"-u", "spetrov",
			"-p", "spetrov",
			"-turl", "jdbc:h2:file:./target/targetDb.h2",
			"-et", "append"
		};

	static String[] exportSqlTargetDbDerby = new String[] {
			"-mode", "targetDb",
			"-sql", "select sysdate, current_date, sys_context('userenv','current_schema') from dual;",
			"-url", "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
			"-u", "spetrov",
			"-p", "spetrov",
			"-turl", "jdbc:derby:target/targetDb.derby;create=true",
			"-et", "truncate"
		};

	public static void main(String[] args) throws Exception {
		String[][] all_args = new String[][] {
			exportTables,
			exportTablesSql,
			exportTablesTargetDb,

			exportSqlTargetDbSQLite,
			exportSqlTargetDbHSQL,
			exportSqlTargetDbH2,

			exportSqlTargetDbDerby,
		};
		args = all_args[3];
		Main.main(args);
	}
}
