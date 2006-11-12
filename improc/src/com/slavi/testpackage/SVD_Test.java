package com.slavi.testpackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;

import com.slavi.matrix.Matrix;

public class SVD_Test {
	
	public static Matrix jamaMat2slaviMat(jama.Matrix ja) {
		Matrix r = new Matrix(ja.getColumnDimension(), ja.getRowDimension());
		for (int i = r.getSizeX() - 1; i >= 0; i--)
			for (int j = r.getSizeY() - 1; j >= 0; j--)
				r.setItem(i, j, ja.get(j, i));
		return r;
	}
	
	public static jama.Matrix slaviMat2jamaMat(Matrix sa) {
		jama.Matrix ja = new jama.Matrix(sa.getSizeY(), sa.getSizeX());
		for (int i = sa.getSizeX() - 1; i >= 0; i--)
			for (int j = sa.getSizeY() - 1; j >= 0; j--)
				ja.set(j, i, sa.getItem(i, j));
		return ja;
	}
	
	public static void printM(Matrix m, String title) {
		System.out.println(title);
		System.out.println(m.toString());
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(
				SVD_Test.class.getResource(
					"SVD-A.txt").getFile()));

		Matrix a = new Matrix(3, 3);
		a.load(fin);
		fin.close();
		Matrix aa = a.makeCopy();
//		a.transpose(aa);
//		aa.copyTo(a);
		
//		jama.Matrix ja = slaviMat2jamaMat(a);
//		jama.SingularValueDecomposition svd = ja.svd();
		
		Matrix u = new Matrix();
		Matrix v = new Matrix();
		Matrix s = new Matrix();

		Matrix tmp1 = new Matrix();
		Matrix tmp2 = new Matrix();
		Matrix tmp3 = new Matrix();
		Matrix tmp4 = new Matrix();

		printM(a, "A");
		a.QRDecomposition(s);
		printM(a, "QR");
		a.getQ(u);
		printM(u, "Q");
		printM(s, "R");
		
//		
//		
//		a.svd2(u, v, s);
//		tmp1.resize(a.getSizeX(), a.getSizeY());
//		for (int i = s.getSizeX() - 1; i >= 0; i--)
//			tmp1.setItem(i, i, s.getItem(i, 0));
//		tmp1.copyTo(s);
//
//		a.save(new PrintStream("c:/temp/a.txt"));
//		u.save(new PrintStream("c:/temp/u.txt"));
//		s.save(new PrintStream("c:/temp/s.txt"));
//		v.save(new PrintStream("c:/temp/v.txt"));
////		Matrix ia = aa.makeCopy();
////		ia.inverse();
////		ia.save(new PrintStream("c:/temp/ia.txt"));
//		
//		
//		printM(u, "U");
//		printM(v, "V");
//		printM(s, "S");
//		
////		Matrix u2 = jamaMat2slaviMat(svd.getU());
////		Matrix v2 = jamaMat2slaviMat(svd.getV());
////		Matrix s2 = jamaMat2slaviMat(svd.getS());
////				
////		printM(u2, "U2");
////		printM(v2, "V2");
////		printM(s2, "S2");
////
////		u.mSub(u2, tmp1);
////		printM(tmp1, "U - U2");
////		v.mSub(v2, tmp1);
////		printM(tmp1, "V - V2");
////		s.mSub(s2, tmp1);
////		printM(tmp1, "S - S2");
//
//		u.transpose(tmp1);
//		tmp1.mMul(u, tmp2);
//		printM(tmp2, "U.' * U");
//		
//		v.transpose(tmp1);
//		v.mMul(tmp1, tmp2);
//		printM(tmp2, "V.' * V");
//
//		printM(aa, "AA");
//
//		u.mMul(s, tmp2);
//		tmp2.mMul(tmp1, tmp3);
//		printM(tmp3, "U * S * V.'");
//		
//		aa.mSub(tmp3, tmp1);
//		printM(tmp1, "AA - U * S * V.'");
	}
}
