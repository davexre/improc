package com.slavi.util;

import org.junit.Test;

import com.slavi.util.CEncoder;
import com.slavi.util.CEncoder.CENCODE;

public class UtilEncodeTest {

	private static final String testStr[] = {
		"Това е на кирилица",
		"\n",
		"",
		"a!2",
		"\\\b\t\f\r\"\''\u1234\\'\\xabfd",
		"\u0003g"
	};

	private void singleTestStr(CENCODE cencode) {
		int i = 0;
		for (String s : testStr) {
			String e = CEncoder.encode(s, cencode);
			String d = CEncoder.decode(e);
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

	@Test
	public void testEncodeDecode() throws Exception {
		for (CENCODE c : CENCODE.values()) {
			singleTestStr(c);
		}
	}
}
