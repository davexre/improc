package com.slavi.jut.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slavi.jut.asm.AsmClass;

public class Destination {

	public String sources;

	public List<String> patterns = new ArrayList();

	public transient String sourcesPath;
	public transient Map<String, AsmClass> classes = new HashMap();
}
