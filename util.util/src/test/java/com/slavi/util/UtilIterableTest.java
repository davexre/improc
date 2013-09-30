package com.slavi.util;

import java.util.StringTokenizer;

import org.junit.Assert;
import org.junit.Test;

public class UtilIterableTest {

	@Test
	public void testIterable() {
		String str = "a b c d e";
		int count = 0;
		for (Object i : Util.iterable(new StringTokenizer(str))) {
			if (i != null)
				count++;
		}
		Assert.assertTrue("", count == 5);
	}
}
