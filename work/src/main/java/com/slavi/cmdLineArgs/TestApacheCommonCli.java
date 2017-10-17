package com.slavi.cmdLineArgs;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import cern.colt.Arrays;

public class TestApacheCommonCli {

	void doIt() throws Exception {
		Options options = new Options();
		options.addOption("s", "string", true, "Specify some string input");
		options.addOption("h", false, "Print this help message");

		Option o = new Option("i", "integers", true, "List of integers");
		o.setArgName("int[, int[, ...]]");
		o.setArgs(Option.UNLIMITED_VALUES);
		o.setOptionalArg(false);
		o.setRequired(false);
		o.setValueSeparator(',');
		o.setType(Integer.class);
		options.addOption(o);

		options.addOption(Option.builder("t")
				.longOpt("things")
				.desc("A comma (NOT column) separated list of stuff")
				.argName("str[, str[, ...]]")
				.numberOfArgs(Option.UNLIMITED_VALUES)
				.valueSeparator(',')
				.build());
		
		options.addOption(Option.builder()
				.longOpt("long")
				.desc("This option has only a long form")
				.build());
		
		CommandLineParser clp = new PosixParser();
		//String args[] = new String[] { "-hi12,13,14"};
		String args[] = new String[] { "-h", "-t", "a", "b, c", "-i12,23,34", "--long"};
		CommandLine cl = clp.parse(options, args);
		if (cl.hasOption("i")) {
			System.out.println(Arrays.toString(cl.getOptionValues("i")));
			//System.out.println(cl.getOptionObject("i"));
		}
		if (cl.hasOption("t")) {
			System.out.println(Arrays.toString(cl.getOptionValues("t")));
			//System.out.println(cl.getOptionObject("i"));
		}
		if (cl.hasOption("long"))
			System.out.println("Has long");
		if (cl.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("myCmd", options);
		}
	}

	public static void main(String[] args) throws Exception {
		new TestApacheCommonCli().doIt();
		System.out.println("Done.");
	}
}
