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
		options.addOption("h", false, "Print this help message");
		Option o = new Option("i", true, "List of integers");
		o.setArgName("int[, int[, ...]]");
		o.setArgs(Option.UNLIMITED_VALUES);
		o.setOptionalArg(false);
		o.setRequired(false);
		o.setValueSeparator(',');
		options.addOption(o);

		/*options.addOption(OptionBuilder
				.withArgName("some int")
				.withDescription("Specify an integer")
				.withLongOpt("integer")
				.hasArg(true)
				.withType(Number.class)
				.create("i"));*/
		
		CommandLineParser clp = new PosixParser();
		//String args[] = new String[] { "-hi12,13,14"};
		String args[] = new String[] { "-hi", ""};
		CommandLine cl = clp.parse(options, args);
		if (cl.hasOption("i")) {
			System.out.println(Arrays.toString(cl.getOptionValues("i")));
			//System.out.println(cl.getOptionObject("i"));
		}
		if (cl.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("asd", options);
		}
	}

	public static void main(String[] args) throws Exception {
		new TestApacheCommonCli().doIt();
		System.out.println("Done.");
	}
}
