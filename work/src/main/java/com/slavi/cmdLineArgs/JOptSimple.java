package com.slavi.cmdLineArgs;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class JOptSimple {

	void doIt() throws Exception {
		String args[] = new String[] { "-e", "-sASD", "more parms", "-t", "a,b ,:c q", "and", "even more", "-tparams " };

		OptionParser parser = new OptionParser( "es:t::h*?*." );
		//OptionParser parser = new OptionParser();
		//parser.accepts("e").description()describedAs("Enable something").defaultsTo(false);
		//parser.accepts("s").with
		OptionSet o = parser.parse(args);
		
		System.out.println(o.has("e"));
		System.out.println(o.valuesOf("t"));
		System.out.println(o.nonOptionArguments());

		parser.printHelpOn(System.out);
	}

	public static void main(String[] args) throws Exception {
		new JOptSimple().doIt();
		System.out.println("Done.");
	}
}
