package com.slavi.util.math;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Util;

public class MatrixTransposedTest {

	@Test
	public void test1() throws Exception {
		Matrix a = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 9");
		Matrix b = a.makeCopy();
		a.transpose();
		Matrix c = a.makeCopy();
		Assert.assertTrue(a.equals(c, 0));
		Assert.assertFalse(b.equals(c, 0));

		double bd[] = b.getVector();
		double cd[] = c.getVector();
		Assert.assertArrayEquals(bd, cd, 0);

		String bs = b.toOneLineString();
		String cs = c.toOneLineString();
		b = Matrix.fromOneLineString(bs);
		c = Matrix.fromOneLineString(cs);
		Assert.assertTrue(a.equals(c));
		Assert.assertTrue(c.equals(a));

		b.transpose();
		Assert.assertTrue(a.equals(b));
		Assert.assertTrue(b.equals(a));

		bd = b.getVector();
		c = b.makeCopy();
		b.loadFromVector(bd);
		Assert.assertTrue(c.equals(b));

		ObjectMapper om = Util.jsonMapper();
		bs = om.writeValueAsString(b);
		cs = om.writeValueAsString(b);
		Assert.assertEquals(bs, cs);

		System.out.println(bs);
	}
}
