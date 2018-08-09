package com.slavi.lang;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;

public class TestStrSubstitutor {

	public void doIt(String[] args) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.putAll(System.getenv());
		map.putAll(new HashMap(System.getProperties()));
		StrSubstitutor ss = new StrSubstitutor(map);

		System.out.println(ss.replace("My home is ${HOME}."));
	}

	public static void main(String[] args) throws Exception {
		new TestStrSubstitutor().doIt(args);
		System.out.println("Done.");
	}
}
