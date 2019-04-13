package com.slavi.dbtools.dbcompare.test;

import com.slavi.dbtools.dbcompare.Main;

public class TestDbCompare {
	static String[] jdbcGetSource = new String[] {
		"get",
		"-f", "target/source.sqlite",
		"-mode", "jdbc",
		"-jdbcschema", "SPETROV",
		"-url", "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
		"-u", "spetrov",
		"-p", "spetrov"
	};

	static String[] jdbcGetTarget = new String[] {
		"get",
		"-f", "target/target.sqlite",
		"-mode", "jdbc",
		"-jdbcschema", "MMITEV",
		"-url", "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
		"-u", "mmitev",
		"-p", "mmitev"
	};

	static String[] oracleGetSource = new String[] {
		"get",
		"-f", "target/source.sqlite",
		"-mode", "oracle",
		"-url", "(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
		"-u", "spetrov",
		"-p", "spetrov",
	};

	static String[] oracleGetTarget = new String[] {
		"get",
		"-f", "target/target.sqlite",
		"-mode", "oracle",
		"-jdbcschema", "MMITEV",
		"-url", "(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
		"-u", "mmitev",
		"-p", "mmitev"
	};

	static String[] compare = new String[] {
		"compare",
		"-f", "target/compare.sqlite",
		"-sdb", "target/source.sqlite",
		"-tdb", "target/target.sqlite"
	};

	static String[] compare_same = new String[] {
			"compare",
			"-f", "target/compare.sqlite",
			"-sdb", "target/source.sqlite",
			"-tdb", "target/source.sqlite"
		};

	public static void main(String[] args) throws Exception {
		String[][] all_args = new String[][] {
			compare,
			compare_same,
			jdbcGetSource,
			jdbcGetTarget,
			oracleGetSource,
			oracleGetTarget
		};
		args = all_args[0];
		Main.main(args);
	}
}
