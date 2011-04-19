package com.slavi.util.file;

import java.util.StringTokenizer;

import org.junit.Test;

import com.slavi.TestUtils;
import com.slavi.util.Util;

public class UtilIterableTest {

	@Test
	public void testIterable() {
		String str = "a b c d e";
		int count = 0;
		for (Object i : Util.iterable(new StringTokenizer(str))) {
			count++;
		}
		TestUtils.assertTrue("", count == 5);
	}
}
