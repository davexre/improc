package com.slavi.math.adjust;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.slavi.math.matrix.Matrix;

public class QQ {
	public static void main(String[] args) throws Exception {
		InputStream is = QQ.class.getResourceAsStream("qq.txt");
		BufferedReader fin = new BufferedReader(new InputStreamReader(is));
		Matrix a = new Matrix(30, 411);
		a.load(fin);
		Matrix q = new Matrix();
		Matrix t = new Matrix(411, 1);
		t.makeR(1);
		a.qrDecomositionGetQ(t, q);
		a.qrDecomositionGetR(a);
		System.out.println(q.toMatlabString("q1"));
		System.out.println(a.toMatlabString("r1"));
	}
}
