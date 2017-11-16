package com.slavi.math.matrix;

import org.junit.Test;
import org.junit.Assert;

public class BitMatrixTest {
	@Test
	public void testBitMatrix() throws Exception {
		BitMatrix m = new BitMatrix(3, 2);
		m.setItem(0, 0, true);
		m.setItem(1, 0, true);
		Assert.assertEquals(m.getItem(0, 0), true);
		Assert.assertEquals(m.getItem(1, 0), true);
		Assert.assertEquals(m.getItem(0, 1), false);
		Assert.assertEquals(m.getItem(1, 1), false);
		
		Assert.assertEquals(m.getItem(2, 1), false);
		Assert.assertEquals(m.itemNot(2, 1), true);
		Assert.assertEquals(m.getItem(2, 1), true);
		
		System.out.println(m);
	}
}
