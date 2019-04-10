package com.slavi.db.dataloader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import cern.colt.Arrays;

public class Main {
	public static int main0(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("h", "help", false, "Display this help");
		options.addOption("cfg", null, true, "Configuration file");
		var opt = new Option("D", "Additional parameter Defines");
		opt.setArgs(2);
		opt.setValueSeparator('=');
		options.addOption(opt);
		options.addOption("url", "", true, "Connect string to database");
		options.addOption("u", "user", true, "User name to connect to database");
		options.addOption("p", "password", true, "Password to connect to database");
		options.addOption("format", null, true, "Input file format. One of \"csv-*\" types.");

		CommandLineParser clp = new DefaultParser();
		boolean showHelp = true;
		CommandLine cl = null;
		try {
			cl = clp.parse(options, args, false);
		} catch (ParseException e) {
			// ignore
		}

		if (cl != null && !cl.hasOption("h")) {
			var vals = cl.getOptionValues('D');
			System.out.println(Arrays.toString(vals));
			showHelp = false;
		}

		if (showHelp) {
			System.out.println(IOUtils.toString(Main.class.getResourceAsStream("Main.help.txt"), "UTF8"));
			return 254;
		}
		return 0;
	}

	public static void main1(String[] args) throws Exception {
		try {
			System.exit(main0(args));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(255);
		}
	}

	public static void main(String[] args) throws Exception {
		Main.main1(new String[] {
			"-Dasd=qwe",
			"-D", "asd1=qwe1",
			"-D", "asd2", "=qwe2",
			"-D", "asd3=", "qwe3",
			"-D", "asd4", "qwe4",
			"-D", "asd5=",
		});
	}
}
