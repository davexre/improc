package com.slavi.db.dataloader;

public class TestDbLoader {
	static String[] exportTables = new String[] {
			"-c",
			"-t", "aa_tmp_change_first_name, user_tables, not_existing_table",
			"-f", "target/output-%t.csv",
			"-url", "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
			"-u", "spetrov",
			"-p", "spetrov"
		};

	public static void main(String[] args) throws Exception {
		String[][] all_args = new String[][] {
			exportTables,
		};
		args = all_args[0];
		Main.main(args);
	}
}
