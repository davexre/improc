package com.slavi.math.matrix;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.junit.Assert;
import org.junit.Test;

public class JLapackMySvdTest {
	public static double precision = 1.0 / 10000.0;

	@Test
	public void testMySvd() throws Exception {
		BufferedReader fin = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("MatrixTest.txt")));
		StringTokenizer stt = new StringTokenizer(fin.readLine());
		Matrix a = new Matrix(Integer.parseInt(stt.nextToken()), Integer.parseInt(stt.nextToken()));
		a.load(fin);
		fin.close();

		Matrix copyA = a.makeCopy();
		Matrix tmp = new Matrix();
		Matrix u = new Matrix();
		Matrix v = new Matrix(a.getSizeX(), a.getSizeX());
		Matrix s = new Matrix(a.getSizeX(), a.getSizeY());

		JLapack jl = new JLapack();
		jl.mysvd(a, u, v, s);
		System.out.println(u.toMatlabString("U"));
		System.out.println(s.toMatlabString("S"));
		System.out.println(v.toMatlabString("V"));


		Matrix checkA = new Matrix();
		u.mMul(s, tmp);
		tmp.mMul(v, checkA);
		System.out.println();
		System.out.println(checkA.toMatlabString("A"));
		checkA.mSub(copyA, a);
		Assert.assertTrue(a.is0(precision));
	}
}
