package com.slavi.lang;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestHashCode {

	void doIt() throws Exception {
		System.out.println(Arrays.hashCode(new int[] { 69, 28 }));
		System.out.println(Arrays.hashCode(new int[] { 68, 59 }));
		System.out.println("ay".hashCode());
		System.out.println("bZ".hashCode());

		Map map = new HashMap();
		map.put(new int[] { 69, 28 }, 1);
		map.put(new int[] { 68, 59 }, 2);
		map.put("ay", 3);
		map.put("bZ", 4);

		System.out.println(map);
	}

	public static void main(String[] args) throws Exception {
		new TestHashCode().doIt();
		System.out.println("Done.");
	}
}
