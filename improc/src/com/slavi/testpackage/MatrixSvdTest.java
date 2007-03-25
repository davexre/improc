package com.slavi.testpackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

import com.slavi.matrix.Matrix;

public class MatrixSvdTest {
	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(
				MatrixTest.class.getResource(
					"SVD-A.txt").getFile()));
		StringTokenizer stt = new StringTokenizer(fin.readLine());
		Matrix a = new Matrix(Integer.parseInt(stt.nextToken()), Integer.parseInt(stt.nextToken()));
		a.load(fin);
		fin.close();
		Matrix at = new Matrix();
		a.transpose(at);
		Matrix b = a.makeCopy();
		Matrix bt = at.makeCopy();
		
		Matrix tmp = new Matrix();

		Matrix u = new Matrix();
		Matrix v = new Matrix(a.getSizeX(), a.getSizeX());
		Matrix s = new Matrix(a.getSizeX(), a.getSizeY());

		Matrix ut = new Matrix();
		Matrix vt = new Matrix();
		Matrix st = new Matrix();

		
		a.svd(s, v);
		a.copyTo(ut);
		s.copyTo(st);
		v.copyTo(vt);
		b.copyTo(a);
		
		a.mysvd(u, v, s);
//		at.mysvd(ut, vt, st);

		
		ut.printM("U1");
		u.printM("U2");
		vt.printM("V1");
		v.printM("V2");
		st.printM("S1");
		s.printM("S2");
				
		
		
//		u.printM("U !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		v.printM("V");
//		s.printM("S");
		
		Matrix checkA = new Matrix();
		Matrix checkAt = new Matrix();

//		ut.printM("UT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		vt.printM("VT");
//		st.printM("ST");
		
		u.mMul(s, tmp);
		tmp.mMul(v, checkA);
		checkA.mSub(b, a);
		a.printM("Diff A");

//		ut.mMul(st, tmp);
//		tmp.mMul(vt, checkAt);
//		checkAt.mSub(bt, at);
//		at.printM("Diff AT");
		
//		ut.termMul(ut, ut);
//		vt.termMul(vt, vt);
//		u.termMul(u, u);
//		v.termMul(v, v);
//				
//		vt.transpose(tmp);
//		tmp.mSub(u, tmp);
//		tmp.printM("Diff U");
//
//		ut.transpose(tmp);
//		tmp.mSub(v, tmp);
//		tmp.printM("Diff V");
//
//		st.transpose(tmp);
//		tmp.mSub(s, tmp);
//		tmp.printM("Diff S");
	}
}
