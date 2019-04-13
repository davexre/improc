package com.slavi.dbtools.dataload.test;

import com.slavi.dbtools.dataload.Main;

public class TestDataLoad {
	static String[] importJson = new String[] {
			"-cfg", TestDataLoad.class.getResource("TestData-jut-config.yml").getFile(),
			"-f", TestDataLoad.class.getResource("TestData-jut.json").getFile(),
		};

	static String[] importXml = new String[] {
			"-cfg", TestDataLoad.class.getResource("TestData-City-config.yml").getFile(),
			"-f", TestDataLoad.class.getResource("TestData-City.xml").getFile(),
		};

	static String[] importCsv = new String[] {
			"-cfg", TestDataLoad.class.getResource("TestData-CSV-config.yml").getFile(),
			"-f", TestDataLoad.class.getResource("TestData-CSV.csv").getFile(),
		};

	static String[] importCsvNoCfg = new String[] {
			"-format", "csv",
			"--formatOptions", "delimiter", "=",
			"--formatOptions", "maxRecordsToCheck=4000",
			"-url", "jdbc:derby:memory:MyDbTest;create=true",
			"-f", TestDataLoad.class.getResource("TestData-CSV.csv").getFile(),
		};

	public static void main(String[] args) throws Exception {
		String[][] all_args = new String[][] {
			importJson,
			importXml,
			importCsv,
			importCsvNoCfg,
		};
		args = all_args[3];
		Main.main(args);
	}
}
