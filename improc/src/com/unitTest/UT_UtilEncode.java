package com.unitTest;

import com.slavi.util.Util;
import com.slavi.util.Util.CENCODE;

public class UT_UtilEncode {

	static final String testStr[] = {
		"Това е на кирилица",
		"\n",
		"",
		"a!2",
		"\\\b\t\f\r\"\''\u1234\\'\\xabfd",
		"\u0003g"
	};
	
	void singleTestStr(CENCODE cencode) {
		int i = 0;
		for (String s : testStr) {
			String e = Util.cEncode(s, cencode);
			String d = Util.cDecode(e);
			if (!s.equals(d)) {
				System.out.println("Failed test string " + i);
				System.out.println("ENCODING " + cencode);
				System.out.println("  Test    string (length " + s.length() + ") is [" + s + "]");
				System.out.println("  Encoded string (length " + e.length() + ") is [" + e + "]");
				System.out.println("  Decoded string (length " + d.length() + ") is [" + d + "]");
				throw new RuntimeException("Failed");
			}
			i++;
		}
	}
	
	void testStrEncodeDecode() {
		for (CENCODE c : CENCODE.values()) {
			singleTestStr(c);
		}
	}
	
	public static void main(String[] args) {
		new UT_UtilEncode().testStrEncodeDecode();
		System.out.println("Done.");
	}
}
