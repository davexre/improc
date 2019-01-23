package com.slavi.jut.draw;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import com.slavi.jut.asm.AsmClass;
import com.slavi.jut.cfg.Config;
import com.slavi.jut.cfg.Location;
import com.slavi.jut.move.JutMove;

public class JutDraw {

	static String formatName(String className) {
		String r = className.replaceAll("/", ".");
		int i = r.lastIndexOf('.');
		return i < 0 ? r : r.substring(0, i) + "\\n" + r.substring(i + 1);
	}

	void drawClass(Config cfg, String patterns[]) throws Exception {
		Map<String, AsmClass> allClasses = new HashMap();
		for (Location location : cfg.locations) {
			location.loadClasses();
			allClasses.putAll(location.declaredClasses);
		}
		Map<String, AsmClass> usedClasses = new HashMap(allClasses);

		Map<String, AsmClass> todo = new HashMap();
		for (String pattern : patterns) {
			Pattern pat = Pattern.compile(pattern);
			for (String c : allClasses.keySet()) {
				if (pat.matcher(c).matches()) {
					AsmClass ac = usedClasses.remove(c);
					todo.put(ac.className, ac);
				}
			}
		}

		PrintStream out = System.out;
		out.println("digraph classes {");

		for (AsmClass ac : todo.values()) {
			String prefix = "  \"" + formatName(ac.className) + "\"";
			out.println(prefix + " [style=dashed];");
		}

		while (todo.size() > 0) {
			Map<String, AsmClass> next = new HashMap();
			for (AsmClass ac : todo.values()) {
				String prefix = "  \"" + formatName(ac.className) + "\" -> \"";
				for (String c2 : ac.usedClasses) {
					if (!allClasses.containsKey(c2) || c2.equals(ac.className))
						continue;
					out.println(prefix + formatName(c2) + "\"");
					AsmClass ac2 = usedClasses.remove(c2);
					if (ac2 == null)
						continue;
					next.put(ac2.className, ac2);
				}
			}
			todo = next;
		}

		out.println("}");
	}

	static String getPackage(String className) {
		int i = className.lastIndexOf('/');
		return i < 0 ? "(default)" : className.substring(0, i).replaceAll("/", ".");
	}

	void drawPackage(Config cfg, String patterns[]) throws Exception {
		Map<String, AsmClass> allClasses = new HashMap();
		for (Location location : cfg.locations) {
			location.loadClasses();
			allClasses.putAll(location.declaredClasses);
		}

		Map<String, Set<String>> packs = new HashMap();
		for (AsmClass ac : allClasses.values()) {
			String pname = getPackage(ac.className);
			Set<String> pack = packs.get(pname);
			if (pack == null) {
				packs.put(pname, pack = new HashSet());
			}
			for (String c2 : ac.usedClasses) {
				if (!allClasses.containsKey(c2))
					continue;
				String pname2 = getPackage(c2);
				if (pname.equals(pname2))
					continue;
				pack.add(pname2);
			}
		}

		Map<String, Set<String>> todo = new HashMap();
		for (String pattern : patterns) {
			Pattern pat = Pattern.compile(pattern);
			for (String c : allClasses.keySet()) {
				String pname = getPackage(c);
				if (pat.matcher(c).matches() || pat.matcher(pname).matches()) {
					Set<String> pack = packs.remove(pname);
					if (pack == null)
						continue;
					todo.put(pname, pack);
				}
			}
		}

		PrintStream out = System.out;
		out.println("digraph packages {");

		for (Map.Entry<String, Set<String>> pack : todo.entrySet()) {
			String prefix = "  \"" + pack.getKey() + "\"";
			out.println(prefix + " [style=dashed];");
		}

		while (todo.size() > 0) {
			Map<String, Set<String>> next = new HashMap();
			for (Map.Entry<String, Set<String>> pack : todo.entrySet()) {
				String prefix = "  \"" + pack.getKey() + "\" -> \"";
				for (String p2 : pack.getValue()) {
					out.println(prefix + p2 + "\"");
					Set<String> pack2 = packs.remove(p2);
					if (pack2 == null)
						continue;
					next.put(p2, pack2);
				}
			}
			todo = next;
		}

		out.println("}");
	}

	public static int main0(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("h", "help", false, "Display this help");
		options.addOption("m", null, true, "Mode: class, package. Default is class.");

		Option o = new Option("l", null, true, "Classes location. May specify more than one.");
		o.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(o);

		o = new Option("r", null, true, "Regex patterns to match classes/packages. May specify more than one.");
		o.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(o);

		CommandLineParser clp = new DefaultParser();
		boolean showHelp = false;
		CommandLine cl = null;
		try {
			cl = clp.parse(options, args, false);
		} catch (ParseException e) {
			// ignore
		}

		Config cfg = new Config();
		cfg.readLocationArgs(cl.getOptionValues('l'));
		String patterns [] = cl.getOptionValues('r');
		if (patterns == null || patterns.length == 0)
			patterns = new String[] { ".*" };
		String mode = cl.getOptionValue('m', "class");

		if (showHelp ||
			cfg.locations.size() == 0) {
			showHelp = true;
		} else {
			cfg.replacePaths();
			if ("class".equals(mode)) {
				new JutDraw().drawClass(cfg, patterns);
			} else if ("package".equals(mode)) {
				new JutDraw().drawPackage(cfg, patterns);
			} else {
				showHelp = true;
			}
		}

		if (showHelp) {
			//HelpFormatter formatter = new HelpFormatter();
			//formatter.printHelp("tools", "", options, "");
			System.out.println(IOUtils.toString(JutDraw.class.getResourceAsStream("JutDraw.help.txt"), "UTF8"));
			return 254;
		}
		return 0;
	}

	public static void main(String[] args) {
		try {
			System.exit(main0(args));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(255);
		}
	}
}
