package com.slavi.util.math;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Util;

public class MatrixTransposedTest {

	String m = "1 2 3; 4 5 6; 7 8 9; 10 11 12";

	@Test
	public void test1() throws Exception {
		Matrix A = Matrix.fromOneLineString(m);

		Matrix a = A.makeCopy();
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
		cs = om.writeValueAsString(c);
		Assert.assertEquals(bs, cs);
	}

	@Test
	public void test2() throws IOException {
		Matrix A = Matrix.fromOneLineString(m);
		Matrix a = A.makeCopy();
		Matrix b = a.transpose(null);
		a.transpose();
		Assert.assertTrue(a.equals(b));

		a.transpose();
		b.transpose();
		Assert.assertTrue(a.equals(b));

		/////////////////
		a = A.makeCopy();
		a.transpose();
		b = a.transpose(null);
		Assert.assertTrue(A.equals(b));

		b = new Matrix();
		a.copyTo(b);
		Assert.assertTrue(a.equals(b));

		b.transpose();
		a.copyTo(b);
		Assert.assertTrue(a.equals(b));

		a.transpose(b);
		b.transpose();
		Assert.assertTrue(a.equals(b));

		///////////////////
		a = A.makeCopy();
		a.transpose();

		String s = a.toOneLineString();
		b = Matrix.fromOneLineString(s);
		Assert.assertTrue(a.equals(b));

		double dd[][] = a.toArray();
		b = Matrix.fromArray(dd);
		Assert.assertTrue(a.equals(b));

		double d[] = a.getVector();
		b = A.makeCopy();
		if (b.isTransposed() != a.isTransposed()) {
			// NOTE: This is a corner case!!! This is the only working case of loadFromVector()
			b.transpose();
		}
		b.loadFromVector(d);
		Assert.assertTrue(a.equals(b));

		////////////////
		a = A.makeCopy();
		a.transpose();

		ObjectMapper om = Util.jsonMapper();
		s = om.writeValueAsString(a);
		b = om.readValue(s, Matrix.class);
		Assert.assertTrue(a.equals(b));

		om = Util.xmlMapper();
		s = om.writeValueAsString(a);
		b = om.readValue(s, Matrix.class);
		Assert.assertTrue(a.equals(b));
	}

}
