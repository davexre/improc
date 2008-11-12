package com.test.util;

import java.util.regex.Pattern;

import com.slavi.util.Util;

public class TestUtil {
	public static void testToRegexpStr(String wildcard, String testStr) {
		String regexp = Util.toRegexpStr(wildcard);
		System.out.print(wildcard + "\t" + testStr + "\t" + regexp + "\t");
		System.out.println(Pattern.matches(regexp, testStr));
	}
	
	public static void main(String[] args) {
		//System.out.println(getDirectory());
		//Object[] values = {"draw mode", "paint mode" };
		//System.out.println(getUIInput(values));
		
		testToRegexpStr("*.*", "aa.bat");
	}
}
