package com.slavi.math.matrix;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.junit.Test;

import com.slavi.TestUtils;

public class JLapackMySvdTest {
	
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
		Matrix checkA = new Matrix();
		u.mMul(s, tmp);
		tmp.mMul(v, checkA);
		checkA.mSub(copyA, a);
		TestUtils.assertMatrix0("", a);
	}
}
