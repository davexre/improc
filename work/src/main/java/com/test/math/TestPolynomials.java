package com.test.math;

public class TestPolynomials {

	public static final int matches[][] = {
	//  {numVariables, power, numCoefs}
		{1, 0, 1},
		{2, 0, 1},
		{3, 0, 1},
		{4, 0, 1},
		{5, 0, 1},
		{6, 0, 1},
		{7, 0, 1},
		{8, 0, 1},
		{9, 0, 1},
		{10, 0, 1},
		{1, 1, 2},
		{1, 2, 3},
		{1, 3, 4},
		{1, 4, 5},
		{1, 5, 6},
		{1, 6, 7},
		{1, 7, 8},
		{1, 8, 9},
		{1, 9, 10},
		{1, 10, 11},
		{2, 1, 3},
		{2, 2, 6},
		{2, 3, 10},
		{2, 4, 15},
		{2, 5, 21},
		{2, 6, 28},
		{2, 7, 36},
		{2, 8, 45},
		{2, 9, 55},
		{2, 10, 66},
		{3, 1, 4},
		{3, 2, 10},
		{3, 3, 20},
		{3, 4, 35},
		{3, 5, 56},
		{3, 6, 84},
		{3, 7, 120},
		{3, 8, 165},
		{3, 9, 220},
		{3, 10, 286},
		{4, 1, 5},
		{4, 2, 15},
		{4, 3, 35},
		{4, 4, 70},
		{4, 5, 126},
		{4, 6, 210},
		{4, 7, 330},
		{4, 8, 495},
		{4, 9, 715},
		{4, 10, 1001},
		{5, 1, 6},
		{5, 2, 21},
		{5, 3, 56},
		{5, 4, 126},
		{5, 5, 252},
		{5, 6, 462},
		{5, 7, 792},
		{5, 8, 1287},
		{5, 9, 2002},
		{5, 10, 3003},
		{6, 1, 7},
		{6, 2, 28},
		{6, 3, 84},
		{6, 4, 210},
		{6, 5, 462},
		{6, 6, 924},
		{6, 7, 1716},
		{6, 8, 3003},
		{6, 9, 5005},
		{6, 10, 8008},
		{7, 1, 8},
		{7, 2, 36},
		{7, 3, 120},
		{7, 4, 330},
		{7, 5, 792},
		{7, 6, 1716},
		{7, 7, 3432},
		{7, 8, 6435},
		{7, 9, 11440},
		{7, 10, 19448},
		{8, 1, 9},
		{8, 2, 45},
		{8, 3, 165},
		{8, 4, 495},
		{8, 5, 1287},
		{8, 6, 3003},
		{8, 7, 6435},
		{8, 8, 12870},
		{8, 9, 24310},
		{8, 10, 43758},
		{9, 1, 10},
		{9, 2, 55},
		{9, 3, 220},
		{9, 4, 715},
		{9, 5, 2002},
		{9, 6, 5005},
		{9, 7, 11440},
		{9, 8, 24310},
		{9, 9, 48620},
		{9, 10, 92378},
		{10, 1, 11},
		{10, 2, 66},
		{10, 3, 286},
		{10, 4, 1001},
		{10, 5, 3003},
		{10, 6, 8008},
		{10, 7, 19448},
		{10, 8, 43758},
		{10, 9, 92378},
		{10, 10, 184756}
	};
	
	public static int getSize(int N, int K) {
		if (K == 0)
			return 1;
		if (N == 1)
			return K + 1;
		return getSize(N - 1, K) + getSize(N, K - 1);
	}
	
	public static int getSize0(int N, int K) {
		int result = 0;
		int pow[] = new int[N];
		
		while (true) {
			int sumPow = 0;
			for (int i = N - 1; i >= 0; i--)
				sumPow += pow[i];
			
			if (sumPow <= K)
				result++;
			
			int p = N - 1;
			while (p >= 0) {
				pow[p]++;
				if (pow[p] <= K)
					break;
				pow[p] = 0;
				p--;				
			}
			if (p < 0)
				break;			
		}
		
		return result;
	}
	
	public static void generateMatches() {
		for (int N = 1; N <=10; N++)
			for (int K = 1; K <=10; K++) {
				int res1 = getSize(N, K);
				int res2 = getSize0(N, K);
				System.out.println((res1 == res2 ? "ok" : "BAD") + "\t" + 
						N + "\t" +
						K + "\t" +
						res1 + "\t" + res2);
			}
	}
	
	public static int getSize2(int N, int K) {
		if (K == 0) return 1;
		if (K == 1) return N + 1;
		double result;
		result = (Math.pow(K + 1, N) + Math.pow(K + 1, N - 1)) / (2 * (N - 1));
		return (int)result;
	}

	public static void check() {
		for (int i = 10; i < matches.length; i++) {
			int res = getSize2(matches[i][0], matches[i][1]);
			System.out.println((res == matches[i][2] ? "ok" : "BAD") + "\t" + 
				matches[i][0] + "\t" +
				matches[i][1] + "\t" +
				matches[i][2] + "\t" + res);
		}
	}
	
	public static void main(String[] args) {
		check();
	}
}
