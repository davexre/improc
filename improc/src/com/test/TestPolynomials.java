package com.test;

public class TestPolynomials {

	public static final int matches[][] = {
	//  {numVariables, power, numCoefs}
		{1, 0, 1},
		{2, 0, 1},
		{3, 0, 1},
		{4, 0, 1},
		
		{1, 1, 2},
		{2, 1, 3},
		{3, 1, 4},
		{4, 1, 5},
		
		{1, 2, 3},
		{2, 2, 6},
		{3, 2, 10},
		
		{1, 3, 4},
		{2, 3, 10},
		{3, 3, 20},
		
		{1, 4, 5},
		{2, 4, 15},
		{3, 4, 35}		
	};
	
	public static int getSize(int N, int K) {
		if (K == 0)
			return 1;
		if (N == 1)
			return K + 1;
		return getSize(N - 1, K) + getSize(N, K - 1);
	}
	
	public static void check() {
		for (int i = 0; i < matches.length; i++) {
			int res = getSize(matches[i][0], matches[i][1]);
			System.out.println((res == matches[i][2] ? "ok" : "BAD") + "\t" + 
				matches[i][0] + "\t" +
				matches[i][1] + "\t" +
				matches[i][2] + "\t" + res);
		}
	}
	
	public static int getSize2(int N, int K) {
		double result;
		result = (Math.pow(K + 1, N - 1) * (K + 2)) / Math.pow(2, N - 1);
		return (int)result;
	}
	
	public static void main(String[] args) {
		for (int N = 1; N <=5; N++)
			for (int K = 1; K <=5; K++) {
				int res1 = getSize(N, K);
				int res2 = getSize2(N, K);
				System.out.println((res1 == res2 ? "ok" : "BAD") + "\t" + 
						N + "\t" +
						K + "\t" +
						res1 + "\t" + res2);
			}
	}
}
