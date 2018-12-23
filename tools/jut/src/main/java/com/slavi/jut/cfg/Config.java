package com.slavi.jut.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

public class Config {

	public Mode mode = Mode.split;

	public List<Location> locations = new ArrayList();

	public List<Destination> destinations = new ArrayList();

	public Destination common = new Destination();

	static void mergeProperties(Properties mergeIntoProperties, Map<?, ?> evaluateProperties) {
		mergeIntoProperties.putAll(evaluateProperties);
		for (Map.Entry<?, ?> entry : evaluateProperties.entrySet()) {
			String propertyName = (String) entry.getKey();
			String value = (String) entry.getValue();
			value = StrSubstitutor.replace(value, mergeIntoProperties);
			mergeIntoProperties.setProperty(propertyName, value);
		}
	}

	public static Properties makeProperties() {
		Properties res = new Properties();
		mergeProperties(res, System.getProperties());
		mergeProperties(res, System.getenv());
		return res;
	}

	public void readLocationArgs(String locationArgs[]) {
		if (locationArgs == null)
			return;
		for (String arg : locationArgs) {
			String vv[] = StringUtils.split(arg, '=');
			if (vv == null)
				continue;

			String sources, classes;
			if (vv.length > 1) {
				classes = StringUtils.trimToEmpty(vv[0]);
				sources = StringUtils.trimToEmpty(vv[1]);
			} else {
				sources = classes = StringUtils.trimToEmpty(arg);
			}
			boolean found = false;
			for (Location i : locations) {
				if (StringUtils.equals(i.classes, classes)) {
					i.sources = sources;
					found = true;
				}
			}
			if (!found) {
				Location item = new Location();
				item.sources = sources;
				item.classes = classes;
				locations.add(item);
			}
		}
	}

	public void readTargetArgs(String targetArgs[]) {
		if (targetArgs == null)
			return;
		for (String arg : targetArgs) {
			String vv[] = StringUtils.split(arg, '=');
			if (vv == null)
				continue;

			String sources, pattern;
			if (vv.length > 1) {
				sources = StringUtils.trimToEmpty(vv[0]);
				pattern = StringUtils.trimToEmpty(vv[1]);
			} else {
				sources = pattern = StringUtils.trimToEmpty(arg);
			}
			boolean found = false;
			for (Destination i : destinations) {
				if (StringUtils.equals(i.sources, sources)) {
					i.patterns.add(pattern);
					found = true;
				}
			}
			if (!found) {
				Destination item = new Destination();
				item.sources = sources;
				item.patterns.add(pattern);
				destinations.add(item);
			}
		}
	}

	public void replacePaths() {
		Properties p = makeProperties();
		for (Location i : locations) {
			i.sourcesPath = StrSubstitutor.replace(i.sources, p);
			i.classesPath = StrSubstitutor.replace(i.classes, p);
		}
		for (Destination i : destinations) {
			i.sourcesPath = StrSubstitutor.replace(i.sources, p);
		}
		common.sourcesPath = StrSubstitutor.replace(common.sources, p);
	}
}
