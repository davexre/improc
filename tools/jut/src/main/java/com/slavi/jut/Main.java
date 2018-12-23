package com.slavi.jut;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.objectweb.asm.ClassReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.slavi.jut.asm.AsmClass;
import com.slavi.jut.cfg.Config;
import com.slavi.jut.cfg.Destination;
import com.slavi.jut.cfg.Location;
import com.slavi.jut.cfg.Mode;

public class Main {

	Map<String, Location> locations = new HashMap();
	Map<String, AsmClass> classes = new HashMap();

	static void mergeProperties(Properties mergeIntoProperties, Map<?, ?> evaluateProperties) {
		mergeIntoProperties.putAll(evaluateProperties);
		for (Map.Entry<?, ?> entry : evaluateProperties.entrySet()) {
			String propertyName = (String) entry.getKey();
			String value = (String) entry.getValue();
			value = StrSubstitutor.replace(value, mergeIntoProperties);
			mergeIntoProperties.setProperty(propertyName, value);
		}
	}

	static Properties makeProperties() {
		Properties res = new Properties();
		mergeProperties(res, System.getProperties());
		mergeProperties(res, System.getenv());
		return res;
	}

	static void loadOneClass(Location l, InputStream classBytes) throws IOException {
		AsmClass r = new AsmClass();
		r.location = l;
		ClassReader cr = new ClassReader(classBytes);
		cr.accept(r.new ClassPrinter(), 0);
		l.declaredClasses.put(r.className, r);
	}

	void loadClassesFromAllLocations(Config cfg) throws Exception {
		for (Location location : cfg.locations) {
			File locationFile = new File(location.classesPath);
			if (locationFile.isDirectory()) {
				for (File i : FileUtils.listFiles(locationFile, null, true)) {
					if (i.isDirectory() || !i.getName().endsWith(".class"))
						continue;
					try (FileInputStream is = new FileInputStream(i)) {
						loadOneClass(location, is);
					}
				}
			} else {
				FileInputStream is = new FileInputStream(locationFile);
				try (ZipInputStream zin = new ZipInputStream(is)) {
					ZipEntry entry = null;
					while ((entry = zin.getNextEntry()) != null) {
						String name = entry.getName();
						if (entry.isDirectory() || !name.endsWith(".class"))
							continue;
						loadOneClass(location, new CloseShieldInputStream(zin));
					}
				}
			}
		}

		// List all classes
		for (Location i : cfg.locations) {
			for (AsmClass ac : i.declaredClasses.values()) {
				AsmClass prev = classes.put(ac.className, ac);
				if (prev != null) {
					// Duplicated class
				}
			}
		}
	}

	static void sortAndPrintCollection(Collection<String> items, String title) {
		System.out.println("##### " + title + " #####");
		ArrayList<String> sorted = new ArrayList(items);
		Collections.sort(sorted);
		for (String i : sorted) {
			System.out.println(i);
		}
	}

	void reportRemaining(Config cfg) {
		Set<String> lines = new HashSet();
		for (Location l : cfg.locations) {
			lines.clear();
			for (String c : new ArrayList<>(l.declaredClasses.keySet())) {
				if (classes.containsKey(c)) {
					lines.add("# " + c);
				}
			}
			sortAndPrintCollection(lines, "Remaining in " + l.sourcesPath);
		}
	}

	void reportDestination(Destination d, String title) {
		// Make directories
		Set<String> lines = new HashSet();
		for (AsmClass i : d.classes.values()) {
			String pkg = FilenameUtils.getPath(i.className);
			String toDir = FilenameUtils.concat(d.sourcesPath, pkg);
			lines.add("mkdir -p '" + toDir + "'");
		}
		sortAndPrintCollection(lines, "Make dirs for " + title);

		lines.clear();
		for (AsmClass i : d.classes.values()) {
			String c = i.className;
			int ci = c.indexOf("$");
			if (ci > 0)
				c = c.substring(0, ci);
			String fromDir = FilenameUtils.concat(i.location.sourcesPath, c);
			String toDir = FilenameUtils.concat(d.sourcesPath, c);
			lines.add("mv '" + fromDir + ".java' '" + toDir + "'");
		}
		sortAndPrintCollection(lines, "Move classes for " + title);
	}

	static boolean moveUsedClasses(AsmClass ac, Map<String, AsmClass> from, Map<String, AsmClass> to) {
		boolean moved = false;
		for (String c : ac.usedClasses) {
			AsmClass ce = from.remove(c);
			if (ce == null) {
				// Class not found
				continue;
			}
			to.put(ce.className, ce);
			moved = true;
		}
		return moved;
	}

	void moveToDest(Config cfg) {
		// Extract classes
		for (Destination d : cfg.destinations) {
			for (String p : d.patterns) {
				Pattern pat = Pattern.compile(p);
				for (String c : new ArrayList<>(classes.keySet())) {
					if (pat.matcher(c).matches()) {
						AsmClass e = classes.remove(c);
						if (e == null) {
							// Class not found
							continue;
						}
						d.classes.put(e.className, e);
						moveUsedClasses(e, classes, d.classes);
					}
				}
			}
		}
	}

	// Extract all referring classes
	public void extractSplit(Config cfg) throws Exception {
		loadClassesFromAllLocations(cfg);

		// Make inverse references
		Map<String, Set<String>> referringClasses = new HashMap<>();
		for (String c : classes.keySet()) {
			referringClasses.put(c, new HashSet<>());
		}
		for (AsmClass ac : classes.values()) {
			for (String cc : ac.usedClasses) {
				Set<String> set = referringClasses.get(cc);
				if (set == null)
					continue;
				set.add(ac.className);
			}
		}

		Destination d = cfg.destinations.get(0);

		moveToDest(cfg);
		boolean moved  = true;
		while (moved) {
			moved = false;
			for (String c : new ArrayList<String>(d.classes.keySet())) {
				Set<String> set = referringClasses.get(c);
				for (String cc : set) {
					AsmClass ce = classes.remove(cc);
					if (ce == null) {
						// Class not found
						continue;
					}
					d.classes.put(ce.className, ce);
					moveUsedClasses(ce, classes, d.classes);
					moved = true;
				}
			}
		}

		reportDestination(d, "Extract");
		reportRemaining(cfg);
	}

	// Extract all used classes
	public void extractCommon(Config cfg) throws Exception {
		loadClassesFromAllLocations(cfg);
		moveToDest(cfg);
		// Make common classes list

		// Check remainging classes
		for (AsmClass e : classes.values()) {
			for (Destination d : cfg.destinations) {
				moveUsedClasses(e, d.classes, cfg.common.classes);
			}
		}

		// Check iteratively all targets
		boolean moved = true;
		while (moved) {
			moved = false;
			for (AsmClass e : new ArrayList<>(cfg.common.classes.values())) {
				for (Destination d : cfg.destinations) {
					moved |= moveUsedClasses(e, d.classes, cfg.common.classes);
				}
			}
		}

		reportDestination(cfg.common, "Common");
		for (Destination d : cfg.destinations) {
			String title = cfg.destinations.size() == 1 ? "Extract" : "Extract to " + d.sources;
			reportDestination(d, title);
		}
		reportRemaining(cfg);
	}

	public static int main0(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("h", "help", false, "Display this help");
		options.addOption("cfg", null, true, "Configuration file");
		Option o = new Option("l", null, true, "Classes location. May specify more than one.");
		o.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(o);

		o = new Option("t", null, true, "Target project location");
		o.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(o);

		o = new Option("m", "", true, "Mode: split, common");
		options.addOption(o);
		options.addOption("c", null, true, "Location from Common classes");

		CommandLineParser clp = new DefaultParser();
		boolean showHelp = false;
		CommandLine cl = null;
		try {
			cl = clp.parse(options, args, false);
		} catch (ParseException e) {
			// ignore
		}

		Config cfg = null;
		if (cl.hasOption("cfg")) {
			File f = new File(cl.getOptionValue("cfg"));
			ObjectMapper m = new ObjectMapper();
			if (f.exists()) {
				try (FileInputStream is = new FileInputStream(f)) {
					cfg = m.readValue(is, Config.class);
				}
			}
		}
		if (cfg == null)
			cfg = new Config();

		if (cl.hasOption('m')) {
			String m = cl.getOptionValue('m');
			switch (m) {
			case "split": cfg.mode = Mode.split; break;
			case "common": cfg.mode = Mode.common; break;
			default: showHelp = true; break;
			}
		}

		String vals[] = cl.getOptionValues('l');
		if (vals != null) {
			for (String val : vals) {
				String vv[] = StringUtils.split(val, '=');
				if (vv == null)
					continue;

				String sources, classes;
				if (vv.length > 1) {
					classes = StringUtils.trimToEmpty(vv[0]);
					sources = StringUtils.trimToEmpty(vv[1]);
				} else {
					sources = classes = StringUtils.trimToEmpty(val);
				}
				boolean found = false;
				for (Location i : cfg.locations) {
					if (StringUtils.equals(i.classes, classes)) {
						i.sources = sources;
						found = true;
					}
				}
				if (!found) {
					Location item = new Location();
					item.sources = sources;
					item.classes = classes;
					cfg.locations.add(item);
				}
			}
		}

		vals = cl.getOptionValues('t');
		if (vals != null) {
			for (String val : vals) {
				String vv[] = StringUtils.split(val, '=');
				if (vv == null)
					continue;

				String sources, pattern;
				if (vv.length > 1) {
					sources = StringUtils.trimToEmpty(vv[0]);
					pattern = StringUtils.trimToEmpty(vv[1]);
				} else {
					sources = pattern = StringUtils.trimToEmpty(val);
				}
				boolean found = false;
				for (Destination i : cfg.destinations) {
					if (StringUtils.equals(i.sources, sources)) {
						i.patterns.add(pattern);
						found = true;
					}
				}
				if (!found) {
					Destination item = new Destination();
					item.sources = sources;
					item.patterns.add(pattern);
					cfg.destinations.add(item);
				}
			}
		}

		if (cl.hasOption('c'))
			cfg.common.sources = cl.getOptionValue('c');

		// Check config
		if (cfg.locations.size() == 0 ||
			cfg.destinations.size() == 0 ||
			(cfg.mode == Mode.split && cfg.destinations.size() != 1)) {
			showHelp = true;
		}

		if (showHelp) {
			//HelpFormatter formatter = new HelpFormatter();
			//formatter.printHelp("tools", "", options, "");
			System.out.println(IOUtils.toString(Main.class.getResourceAsStream("Main.help.txt"), "UTF8"));
			return 254;
		} else {
			Properties p = makeProperties();
			for (Location i : cfg.locations) {
				i.sourcesPath = StrSubstitutor.replace(i.sources, p);
				i.classesPath = StrSubstitutor.replace(i.classes, p);
			}
			for (Destination i : cfg.destinations) {
				i.sourcesPath = StrSubstitutor.replace(i.sources, p);
			}
			cfg.common.sourcesPath = StrSubstitutor.replace(cfg.common.sources, p);

			if (cfg.mode == Mode.split) {
				new Main().extractSplit(cfg);
			} else {
				new Main().extractCommon(cfg);
			}

			String cfgName = StringUtils.trimToNull(cl.getOptionValue("cfg"));
			if (cfgName == null && (!cl.hasOption("cfg")))
				cfgName = "jut-cfg.json";
			if (cfgName != null) {
				try (FileOutputStream out = new FileOutputStream(cfgName)) {
					ObjectMapper m = new ObjectMapper();
					m.enable(SerializationFeature.INDENT_OUTPUT);
					m.writeValue(out, cfg);
				}
			}
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
