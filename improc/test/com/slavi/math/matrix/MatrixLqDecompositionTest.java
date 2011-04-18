package com.slavi.math.matrix;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.junit.Test;

import com.slavi.TestUtils;

public class MatrixLqDecompositionTest {

	@Test
	public void testLqDecomposition() throws Exception {
		BufferedReader fin = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("MatrixTest.txt")));
		StringTokenizer stt = new StringTokenizer(fin.readLine());
		Matrix a = new Matrix(Integer.parseInt(stt.nextToken()), Integer.parseInt(stt.nextToken()));
		a.load(fin);
		fin.close();
		
		Matrix at = new Matrix();
		a.transpose(at);
		Matrix b = a.makeCopy();
		Matrix tmp = new Matrix();

		Matrix u = new Matrix();
		Matrix v = new Matrix(a.getSizeX(), a.getSizeX());
		Matrix s = new Matrix(a.getSizeX(), a.getSizeY());

		a.lqDecomposition(tmp);
		a.lqDecomositionGetQ(tmp, u);
		a.lqDecomositionGetL(v);

//		u.printM("U2");
//		v.printM("V2");
		v.mMul(u, s);
//		s.printM("S2");
		s.mSub(b, b);
//		b.printM("A");
		TestUtils.assertMatrix0("", b);
	}
}
