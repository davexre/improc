package com.test.math;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

import com.slavi.math.matrix.JLapack;
import com.slavi.math.matrix.Matrix;

public class TestJLapack {
	
	public static void main(String[] args) throws Exception {
		JLapack jl = new JLapack();
		Matrix wri = new Matrix();
		Matrix p = new Matrix(4, 1);
		p.setItem(0, 0, 1);
		p.setItem(1, 0, -6);
		p.setItem(2, 0, -72);
		p.setItem(3, 0, -27);
		jl.roots(p, wri);
		wri.printM("Result=");
	}
	
	public static void main2(String[] args) throws Exception {
		JLapack jl = new JLapack();

		BufferedReader fin = new BufferedReader(new FileReader(
				TestMatrix2.class.getResource(
					"SVD-A.txt").getFile()));
		StringTokenizer stt = new StringTokenizer(fin.readLine());
		Matrix theMatrix = new Matrix(Integer.parseInt(stt.nextToken()), Integer.parseInt(stt.nextToken()));
		theMatrix.load(fin);
		fin.close();

		Matrix a = theMatrix.makeCopy();
		theMatrix.transpose(a);

//		Matrix scale = new Matrix();
		a.printM("A");
		//jl.DGEBAL(a, scale, jl.DGEBAL_result);
//		Matrix Left = new Matrix();
		Matrix WRI = new Matrix();
		jl.DGEEV(a, WRI);
		a.printM("A");
/*
		Matrix tmp = new Matrix();
		a.transpose(tmp);
		a = tmp.makeCopy();

		Matrix b = a.makeCopy();
		
		Matrix aU = new Matrix(); 
		Matrix aS = new Matrix();
		Matrix aV = new Matrix();

		Matrix bU = new Matrix();
		Matrix bS = new Matrix();
		Matrix bV = new Matrix();
		
		Matrix dU = new Matrix();
		Matrix dS = new Matrix();
		Matrix dV = new Matrix();
		
		//a.mysvd(aU, aV, aS);
		jl.mysvd(a, aU, aV, aS);
		
//		aU.mSub(bU, dU);
//		dU.printM("Diff U");
//		
//		aV.mSub(bV, dV);
//		dV.printM("Diff V");
//		
//		aS.mSub(bS, dS);
//		dS.printM("Diff S");
		
		// Check SVD result
		Matrix tmpa = new Matrix();
		Matrix tmpb = new Matrix();
		aU.mMul(aS, tmpa);
		tmpa.mMul(aV, tmpb);
		tmpb.mSub(tmp, tmpa);
		tmpa.printM("U * S * V - theMatrix");
*/
	}
}
