package com.test.util;

import com.slavi.util.DumpUtil;

public class TestDumpUtils {
	public static String str = "alabala";
	
	public static void main(String[] args) {
		TestDumpUtils du = new TestDumpUtils();
		DumpUtil.showObject(du, false, false);
	}
}
