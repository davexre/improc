package com.slavi.cmdLineArgs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.IParameterSplitter;

public class TestJCommander {
	// http://pholser.github.io/jopt-simple/

	public static class MyParameterSplitter implements IParameterSplitter {
		public List<String> split(String value) {
			return Arrays.asList(value.split("[,:\\s]+"));
		}
	}

	@Parameters(commandDescription = "My command", separators = ":=\\s")
	public static class MyArgs {
		
		@Parameter(names = {"-h", "-help", "--help"}, description = "Prints this help message", help = true)
		boolean help = false;
		
		// This option will go at the end of the help message since it does not have a "long" version.
		@Parameter(names = "-e", description = "Enable something")
		boolean enable = false;
		
		@Parameter(names = {"-s", "--string"}, description = "Specify some string input")
		String str;
		
		@Parameter(names = {"-t", "--things"}, description = "A comma or column separated list of stuff", splitter = MyParameterSplitter.class)
		List<String> items = new ArrayList<>();
		
		@Parameter(description = "Other arguments")
		List<String> more = new ArrayList<>();
		
		public String toString() {
			return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}	
	
	void doIt() throws Exception {
		String args[] = new String[] { "-e", "-s ASD", "more parms", "-t", "a,b ,:c q", "and", "even more", " params ", " -h" };
		MyArgs myArgs = new MyArgs();
		JCommander jcmd = JCommander.newBuilder().addObject(myArgs).programName("myCmd").build();
		try {
			jcmd.parse(args);
		} catch (ParameterException e) {
			e.printStackTrace();
			myArgs.help = true;
		}
		System.out.println(myArgs);
		System.out.println(myArgs.items.size());
		if (myArgs.help) {
			jcmd.usage();
		}
	}

	public static void main(String[] args) throws Exception {
		new TestJCommander().doIt();
		System.out.println("Done.");
	}
}
