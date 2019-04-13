package com.slavi.dbtools.dataload;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.app.VelocityEngine;

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
		options.addOption("f", "file", true, "Password to connect to database");
		options.addOption(null, "format", true, "Input file format. One of \"csv-*\" types.");
		opt = new Option(null, "formatOptions", true, "Format options");
		opt.setArgs(2);
		opt.setValueSeparator('=');
		options.addOption(opt);

		CommandLineParser clp = new DefaultParser();
		CommandLine cl = null;
		try {
			cl = clp.parse(options, args, false);
		} catch (ParseException e) {
			// ignore
		}

		boolean showHelp = true;
		if (cl == null || cl.hasOption("h")) {
			// Show help;
		} else if (cl.hasOption("cfg") && cl.hasOption("format")) {
			System.out.println("Invalid combination of options specified -cfg and -format");
		} else {
			VelocityEngine ve = DataLoad.makeVelocityEngine();
			Config cfg;
			if (cl.hasOption("cfg")) {
				try (var is = new FileInputStream(cl.getOptionValue("cfg"))) {
					cfg = DataLoad.loadConfig(ve , is);
				}
			} else {
				cfg = new Config(ve);
				cfg.setFormat(cl.getOptionValue("format", "csv"));
				cfg.defs = new ArrayList();

				var map = new HashMap();
				var vals = cl.getOptionValues("formatOptions");
				int i = 0;
				while (vals != null && i < vals.length) {
					map.put(vals[i++], vals[i++]);
				}
				cfg.setFormatOptions(map);
			}

			var vals = cl.getOptionValues('D');
			int i = 0;
			while (vals != null && i < vals.length) {
				cfg.getVariables().put(vals[i++], vals[i++]);
			}

			if (cl.hasOption("url")) cfg.setUrl(cl.getOptionValue("url"));
			if (cl.hasOption("u")) cfg.setUsername(cl.getOptionValue("u"));
			if (cl.hasOption("p")) cfg.setPassword(cl.getOptionValue("p"));
			String dataFile = cl.getOptionValue("f");
			InputStream fis = dataFile != null ? new FileInputStream(dataFile) : System.in;
			try {
				new DataLoad().loadData(cfg, fis, dataFile);
			} finally {
				if (dataFile != null)
					IOUtils.closeQuietly(fis);
			}

			showHelp = false;
		}

		if (showHelp) {
			System.out.println(IOUtils.toString(Main.class.getResourceAsStream("Main.help.txt"), "UTF8"));
			return 254;
		}
		return 0;
	}

	public static void main(String[] args) throws Exception {
		try {
			System.exit(main0(args));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(255);
		}
	}
}
