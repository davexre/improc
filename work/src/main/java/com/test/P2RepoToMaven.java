package com.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class P2RepoToMaven {

	String p2repo = "/home/slavian/temp/eclipse.platform.4.6.2/org.eclipse.platform-4.6.2";
	
	public static class Bundle {
		public File file;
		public String lengthiestClassName;
		
		public String bundleName;
		public String bundleSymbolicName;
		public String bundleVersion;
		public String exportPackage;
		public String embededArtifacts;
		public String requireBundleStr;
		public Map<String, Map<String, String>> requireBundle;
		
	}
	
	static Map<String, Map<String, String>> parseRequireBundle(String requireBundle) {
		Map<String, Map<String, String>> r = new HashMap<>();
		
		int index = 0;
		int state = 0;
		Map<String, String> params = new HashMap<>();
		String token = "";
		String paramName = "";
		char endChar = ';';
		while (index < requireBundle.length()) {
			char c = requireBundle.charAt(index++);
			switch (state) {
			case 0:
				switch (c) {
				case ';':
					state = 1;
					// falls through
				case ',':
					params = new HashMap<>();
					token = StringUtils.trimToEmpty(token);
					if (!"".equals(token))
						r.put(token, params);
					token = "";
					break;
				default:
					token += c;
					break;
				}
				break;
				
			case 1:
				switch (c) {
				case '=':
					paramName = StringUtils.trimToEmpty(token);
					token = "";
					endChar = ';';
					state = 2;
					break;
				case ',':
					paramName = "";
					token = "";
					state = 0;
					break;
				default:
					token += c;
					break;
				}
				break;
			
			case 2:
				switch (c) {
				case '\'':
				case '"':
					if (endChar != c) {
						endChar = c;
						break;
					} else
						endChar = ';';
					// fall through
				case ',':
				case ';':
					if (endChar == ';') {
						params.put(paramName, StringUtils.trimToEmpty(token));
						paramName = "";
						token = "";
						state = c == ';' ? 1 : 0;
						break;
					}
					// else fall through 
				default:
					token += c;
					break;
				}
			}
		};
		
		token = StringUtils.trimToEmpty(token);
		paramName = StringUtils.trimToEmpty(paramName);
		switch (state) {
		case 0:
			if (!"".equals(token))
				r.put(token, params);
			break;
		case 1:
		case 2:
			if (!"".equals(paramName))
				params.put(paramName, token);
			break;
		}
		return r;
	}
	
	static Bundle getBundleInfoFromManifest(Manifest m) throws Exception {
		Bundle b = new Bundle();
		Attributes a = m.getMainAttributes();
		b.bundleSymbolicName = StringUtils.trimToEmpty(a.getValue("Bundle-SymbolicName")).split(";")[0];
		b.bundleName = StringUtils.trimToEmpty(a.getValue("Bundle-Name"));
		b.bundleVersion = StringUtils.trimToEmpty(a.getValue("Bundle-Version"));
		b.exportPackage = StringUtils.trimToEmpty(a.getValue("Export-Package"));
		b.embededArtifacts = StringUtils.trimToEmpty(a.getValue("Embeded-Artifacts"));
		b.requireBundleStr = StringUtils.trimToEmpty(a.getValue("Require-Bundle"));
		b.requireBundle = parseRequireBundle(b.requireBundleStr);
		return b;
	}
	
	static Bundle getBundleInfoFromZip(InputStream is) throws Exception {
		Bundle b = null;
		String lengthiestClassName = "";
		ZipEntry ze = null;
		try (ZipInputStream zis = new ZipInputStream(is)) {
			while ((ze = zis.getNextEntry()) != null) {
				String name = ze.getName();
				if (name.endsWith(".class")) {
					String className = name.replaceAll("/|\\\\", ".");
					if (lengthiestClassName.length() < className.length())
						lengthiestClassName = className;
				} else if ("META-INF/MANIFEST.MF".equals(name)) {
					Manifest mf = new Manifest(new CloseShieldInputStream(zis));
					b = getBundleInfoFromManifest(mf);
				}
				//zis.skip(Long.MAX_VALUE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		if (b != null)
			b.lengthiestClassName = lengthiestClassName;
		return b;
	}
	
	static Bundle getBundleInfoFromJar(File f) throws Exception {
		try (InputStream is = new FileInputStream(f)) {
			System.out.println(f);
			Bundle b = getBundleInfoFromZip(is);
			if (b != null)
				b.file = f;
			return b;
		}
	}

	Map<String, Bundle> bundles = new HashMap<>();
	
	void processRepo(String p2repo) throws Exception {
		File files[] = new File(p2repo, "plugins").listFiles();
		for (File f : files) {
			if (f.isFile() && f.getName().endsWith(".jar")) {
				Bundle b = getBundleInfoFromJar(f);
				if (b == null)
					continue;
				if (bundles.containsKey(b.bundleSymbolicName))
					continue;
				bundles.put(b.bundleSymbolicName, b);
			}
		}
	}
	
	void checkMissingBundles() {
		for (Bundle b : bundles.values()) {
			//System.out.println(b.bundleSymbolicName +  ":" + b.file.getName() + ":" + b.requireBundle.keySet());
			
			for (String rbName : b.requireBundle.keySet()) {
				Bundle rb = bundles.get(rbName);
				if (rb == null) {
					System.out.println(b.file + " needs bundle " + rbName);
				}
			}
		}
	}
	
	public static class BundleAvailableInMaven {
		public String bundleSymbolicName;
		public String bundleVersion;
		public String mavenArtefact;
	}
	Map<String, BundleAvailableInMaven> availableInMaven = new HashMap<>();
	
	Comparator<Bundle> bundleComparator = new Comparator<Bundle>() {
		public int compare(Bundle o1, Bundle o2) {
			return o1.bundleSymbolicName.compareTo(o2.bundleSymbolicName);
		}
	};
	
	void printBundles() {
		List<Bundle> listBundles = new ArrayList<>();
		listBundles.addAll(bundles.values());
		Collections.sort(listBundles, bundleComparator);

		List<Bundle> requiredBundles = new ArrayList<>();
		for (Bundle b : listBundles) {
			if (availableInMaven.containsKey(b.bundleSymbolicName))
				continue;
			System.out.println(b.bundleSymbolicName + ":" + b.bundleVersion + ":" + b.lengthiestClassName);

			requiredBundles.clear();
			calcRequiredBundles(b, requiredBundles);
			Collections.sort(requiredBundles, bundleComparator);
			for (Bundle rb : listBundles) {
				System.out.println("\t" + rb.bundleSymbolicName);
			}
		}
	}
	
	void calcRequiredBundles(Bundle b, List<Bundle> required) {
		for (String rbName : b.requireBundle.keySet()) {
			Bundle rb = bundles.get(rbName);
			if (rb == null) {
				System.out.println("\t>>>> " + b.file + " needs bundle " + rbName);
				continue;
			}
			if (required.contains(rb))
				continue;
			required.add(rb);
			calcRequiredBundles(rb, required);
		}
	}
	
	void doIt() throws Exception {
		getBundleInfoFromJar(new File(p2repo, "plugins/org.eclipse.swt.gtk.linux.s390x_3.105.2.v20161122-0613.jar"));
		
		
		ObjectMapper mapper = new ObjectMapper();
		Map<Object, Object> map = mapper.readValue(getClass().getResourceAsStream("P2RepoToMaven.json"), Map.class);
		
		map = (Map) map.get("availableInMaven");
		for (Map.Entry i : map.entrySet()) {
			String bundle = (String) i.getKey();
			String tmp[] = bundle.split(":", 2);
			BundleAvailableInMaven b = new BundleAvailableInMaven();
			b.bundleSymbolicName = StringUtils.trimToEmpty(tmp[0]);
			b.bundleVersion = StringUtils.trimToEmpty(tmp.length > 1 ? tmp[1] : "");
			b.mavenArtefact = StringUtils.trimToEmpty((String) i.getValue());
			availableInMaven.put(b.bundleSymbolicName, b);
		}
		
		processRepo(p2repo);
		printBundles();
	}

	public static void main(String[] args) throws Exception {
		new P2RepoToMaven().doIt();
		System.out.println("Done.");
	}
}
