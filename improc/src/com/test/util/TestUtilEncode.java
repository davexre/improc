package com.test.util;

import com.slavi.util.Util;

public class TestUtilEncode {

	static final String testStr[] = {
		"Това е на кирилица",
		"\n",
		"",
		"a!2",
		"\\\b\t\f\r\"\''\u1234\\'\\xabfd",
		"\u0003g"
	};
	
	public static void main(String[] args) {
		int i = 0;
		for (String s : testStr) {
			String e = Util.cEncode(s);
			String d = Util.cDecode(e);
			if (!s.equals(d)) {
				System.out.println("Failed test string " + i);
				System.out.println("  Test    string (length " + s.length() + ") is [" + s + "]");
				System.out.println("  Encoded string (length " + e.length() + ") is [" + e + "]");
				System.out.println("  Decoded string (length " + d.length() + ") is [" + d + "]");
			}
			i++;
		}
		System.out.println("Done.");
	}
}
