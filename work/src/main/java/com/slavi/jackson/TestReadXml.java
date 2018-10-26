package com.slavi.jackson;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.ann.test.Utils;

public class TestReadXml {

	public void doIt(String[] args) throws Exception {
		ObjectMapper m = Utils.xmlMapper();
		Map map = m.readValue(getClass().getResourceAsStream("TestReadXml.xml"), Map.class);
		System.out.println(map);
	}

	public static void main(String[] args) throws Exception {
		new TestReadXml().doIt(args);
		System.out.println("Done.");
	}
}
