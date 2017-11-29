package com.slavi.cmdLineArgs;

import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class JOptSimple {

	OptionParser makeParserShort() {
		OptionParser parser = new OptionParser( "es:t::h*?*." );
		return parser;
	}

	OptionParser makeParserLong() {
		OptionParser parser = new OptionParser();
		parser.acceptsAll(Arrays.asList("h", "help"), "Print this help message").forHelp();
		parser.accepts("e", "Enable something"); //.withOptionalArg().ofType(boolean.class).defaultsTo(false);
		parser.acceptsAll(Arrays.asList("s", "string"), "Specify some string input").withOptionalArg().ofType(String.class); //.defaultsTo(null);
		parser.acceptsAll(Arrays.asList("t", "things"), "A comma or column separated list of stuff").withOptionalArg().ofType(String.class).withValuesSeparatedBy(",: \t\n\r\f"); //.defaultsTo(null);
		parser.accepts("w", "Array of strings with default [\"default\", \"string\", \"array\"]").withOptionalArg().ofType(String.class).defaultsTo("default", "string", "array");
		//.withOptionalArg().ofType(boolean.class).defaultsTo(false);
		return parser;
	}
	
	void doIt() throws Exception {
		String args[] = new String[] { "-e", "-sASD", "more parms", "-t", "a,b ,:c q", "and", "even more", "-tparams ", "-h" };

		OptionParser parser = makeParserLong();
		OptionSet o = parser.parse(args);
		
		
		System.out.println("-h -> " + o.has("h"));
		System.out.println("-e -> " + o.has("e"));
		System.out.println("-s -> " + o.valueOf("s"));
		System.out.println("-t -> " + o.valuesOf("t"));
		System.out.println("-w -> " + o.valuesOf("w"));
		System.out.println("others -> " + o.nonOptionArguments());

		parser.printHelpOn(System.out);
	}

	public static void main(String[] args) throws Exception {
		new JOptSimple().doIt();
		System.out.println("Done.");
	}
}
