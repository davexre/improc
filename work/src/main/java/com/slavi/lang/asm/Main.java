package com.slavi.lang.asm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.text.StrSubstitutor;

import com.slavi.util.PropertyUtil;

public class Main {

	Map<File, ClassLocation> locations = new HashMap();
	Map<String, AsmClass> classes = new HashMap();

	Map<String, AsmClass> toExtract = new HashMap();
	Map<String, AsmClass> toCommon = new HashMap();

	public void readAllClassLocations(String[] locationsToRead) throws Exception {
		Properties p = PropertyUtil.makeProperties();
		for (String i : locationsToRead) {
			File f = new File(StrSubstitutor.replace(i, p));
			if (locations.get(f) != null)
				continue;
			ClassLocation cl = new ClassLocation(f);
			cl.processClassLocation();
			locations.put(f, cl);
		}

		// List all classes
		for (ClassLocation cl : locations.values()) {
			for (AsmClass ac : cl.declaredClasses.values()) {
				AsmClass prev = classes.put(ac.className, ac);
				if (prev != null) {
					// Duplicated class
				}
			}
		}
	}

	static void sortAndPrintCollection(Collection<String> items, String title) {
		System.out.println("----- " + title + " -----");
		ArrayList<String> sorted = new ArrayList(items);
		Collections.sort(sorted);
		for (String i : sorted) {
			System.out.println(i);
		}
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

	// Extract all referring classes
	public void doIt() throws Exception {
		readAllClassLocations(locationsStr);

		String classesToExtract[] = {
			"com/slavi/ann/test/v2/connection/ConvolutionLayer"
		};

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

		// Extract classes
		for (String c : classesToExtract) {
			AsmClass e = classes.remove(c);
			if (e == null) {
				// Class not found
				continue;
			}
			toExtract.put(e.className, e);
		}

		boolean moved  = true;
		while (moved) {
			moved = false;
			for (String c : new ArrayList<String>(toExtract.keySet())) {
				Set<String> set = referringClasses.get(c);
				for (String cc : set) {
					AsmClass ce = classes.remove(cc);
					if (ce == null) {
						// Class not found
						continue;
					}
					toExtract.put(ce.className, ce);
					moveUsedClasses(ce, classes, toExtract);
					moved = true;
				}
			}
		}

		sortAndPrintCollection(toExtract.keySet(), "To extract");
		sortAndPrintCollection(this.classes.keySet(), "Remaining");
	}

	// Extract all used classes
	public void doIt2() throws Exception {
		readAllClassLocations(locationsStr);

		String classesToExtract[] = {
			"com/slavi/ann/test/v2/connection/ConvolutionLayer"
		};

		// Extract classes
		for (String c : classesToExtract) {
			AsmClass e = classes.remove(c);
			if (e == null) {
				// Class not found
				continue;
			}
			toExtract.put(e.className, e);
			moveUsedClasses(e, classes, toExtract);
		}

		// Make common classes list
		for (AsmClass e : classes.values()) {
			moveUsedClasses(e, toExtract, toCommon);
		}

		sortAndPrintCollection(toExtract.keySet(), "To extract");
		sortAndPrintCollection(toCommon.keySet(), "To common");
		sortAndPrintCollection(this.classes.keySet(), "Remaining");
	}

	static String locationsStr[] = {
		"${user.home}/.m2/repository/com/slavi/util.math/1.0.0-SNAPSHOT/util.math-1.0.0-SNAPSHOT.jar",
		"${user.home}/.m2/repository/com/slavi/util.util/1.0.0-SNAPSHOT/util.util-1.0.0-SNAPSHOT.jar",
		"${user.home}/.m2/repository/com/slavi/util.dbutil/1.0.0-SNAPSHOT/util.dbutil-1.0.0-SNAPSHOT.jar",
		"${user.home}/.m2/repository/com/slavi/util.io/1.0.0-SNAPSHOT/util.io-1.0.0-SNAPSHOT.jar",
		"${IMPROC_HOME}/work/target/classes",
	};

	public static void main(String[] args) throws Exception {
		new Main().doIt();
		System.out.println("Done.");
	}
}
