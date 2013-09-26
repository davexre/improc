package com.slavi.util;

import java.util.StringTokenizer;

import org.junit.Test;

import com.slavi.util.Util;
import com.slavi.util.testUtil.TestUtil;

public class UtilIterableTest {

	@Test
	public void testIterable() {
		String str = "a b c d e";
		int count = 0;
		for (Object i : Util.iterable(new StringTokenizer(str))) {
			if (i != null)
				count++;
		}
		TestUtil.assertTrue("", count == 5);
	}
}
