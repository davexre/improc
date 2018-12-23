package com.slavi.jut.cfg;

import java.util.HashMap;
import java.util.Map;

import com.slavi.jut.asm.AsmClass;

public class Location {

	public String classes;

	public String sources;

	public transient String classesPath;
	public transient String sourcesPath;
	public transient Map<String, AsmClass> declaredClasses = new HashMap();
}
