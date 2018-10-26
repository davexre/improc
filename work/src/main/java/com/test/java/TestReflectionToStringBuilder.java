package com.test.java;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.slavi.ann.test.Utils;

public class TestReflectionToStringBuilder {

	public Map map1 = new HashMap();

	public void doIt(String[] args) throws Exception {
		Map map2 = new HashMap();
		map1.put("a", "A");
		map2.put("b", "B");
		map1.put("map2", map2);
		//map2.put("map1", map1);

		System.out.println(Utils.jsonMapper().writeValueAsString(this));
		System.out.println(ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE));
	}

	public static void main(String[] args) throws Exception {
		new TestReflectionToStringBuilder().doIt(args);
		System.out.println("Done.");
	}
}
