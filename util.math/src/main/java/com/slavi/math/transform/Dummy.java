package com.slavi.math.transform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import com.slavi.math.matrix.Matrix;

public class Dummy {

	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new InputStreamReader(Dummy.class.getResourceAsStream("data.txt")));
		Matrix m = new Matrix(4, 800);
		Matrix f = new Matrix(1, 800);
		for (int j = 0; j < m.getSizeY(); j++) {
			StringTokenizer st = new StringTokenizer(fin.readLine());
			f.setItem(0, j, Double.parseDouble(st.nextToken()));
			for (int i = 0; i < m.getSizeX(); i++) 
				m.setItem(i, j, Double.parseDouble(st.nextToken()));
		}
		fin.readLine();
		Matrix nm = new Matrix(4,4);
		nm.load(fin);
		fin.readLine();
		Matrix nmInv = new Matrix(4,4);
		nmInv.load(fin);		
		
		Matrix mTr = new Matrix();
		m.transpose(mTr);
		Matrix nm2 = new Matrix();
		mTr.mMul(m, nm2);
		
		nm.printM("NM");
		System.out.println("NM");
		nm.save(System.out);
		nm2.printM("NM2");		
		System.out.println("NM2");
		nm2.save(System.out);
		
		Matrix dif = new Matrix();
		nm2.mSub(nm, dif);
		dif.printM("dif NM");
		
		Matrix nmInv2 = nm.makeCopy();
		nmInv2.inverse();
		nmInv2.mSub(nmInv, dif);
		dif.printM("dif NM'");
		
		nm.mMul(nmInv, dif);
		dif.printM("NM * NM'");

		nm2.mMul(nmInv2, dif);
		dif.printM("NM2 * NM2'");
		
		fin.close();
	}
}
