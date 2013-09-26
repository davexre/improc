package com.slavi.math.matrix;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;

import com.slavi.util.testUtil.TestUtil;

public class SymmetricMatrixTest {

	Matrix m;
	
	SymmetricMatrix tm;
	
	@Before
	public void setUp() throws Exception {
		BufferedReader fin = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("SymmetricMatrixTest.txt")));
		m = new Matrix(3, 3);
		tm = new SymmetricMatrix(3);
		m.load(fin);
		fin.readLine();
		tm.load(fin);
		fin.close();		
	}

	@Test
	public void testMatrixInverse() {
		Matrix a = m.makeCopy();
		Matrix b = m.makeCopy();
		TestUtil.assertTrue("Failed to inverse the matrix", a.inverse());
		a.mMul(m, b);
		TestUtil.assertMatrixE("Inverse matrix incorrect", b);
	}

	@Test
	public void testTriangularMatrixInverse() throws Exception {
		SymmetricMatrix ta = tm.makeCopy();
		SymmetricMatrix tb = tm.makeCopy();
		TestUtil.assertTrue("Failed to inverse the matrix", ta.inverse());
		ta.mMul(tm, tb);
		TestUtil.assertMatrixE("Inverse matrix incorrect", tb.makeSquareMatrix());
	}
}
