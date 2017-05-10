package com.kattis.solution;

import java.io.BufferedInputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Solution1 {

	public void doIt(Scanner scanner) throws Exception {
		int caseCounter = 0;
		while (scanner.hasNext()) {
			int rows = scanner.nextInt();
			int cols = scanner.nextInt();
			scanner.nextLine();
			int curLine[] = new int[cols];
			int prevLine[] = new int[cols];
			int starId = 0;
			Set<Integer> starsSet = new HashSet<>();
			for (int j = 0; j < cols; j++)
				prevLine[j] = 0;
			for (int i = 0; i < rows; i++) {
				String lineStr = scanner.nextLine();
				boolean prevPixel = false;
				for (int j = 0; j < cols; j++) {
					boolean pixel = lineStr.charAt(j) == '-';
					if (pixel) {
						if (!prevPixel) {
							starsSet.add(++starId);
						}
						curLine[j] = starId;
						int aboveId = prevLine[j];
						if (aboveId != 0 && aboveId != starId) {
							starsSet.remove(aboveId);
							for (int k = 0; k < cols; k++)
								if (prevLine[k] == aboveId)
									prevLine[k] = starId;
							for (int k = 0; k < j; k++)
								if (curLine[k] == aboveId) {
									curLine[k] = starId;
								}
						}
					} else {
						curLine[j] = 0;
					}
					prevPixel = pixel;
				}
				int tmp[] = prevLine;
				prevLine = curLine;
				curLine = tmp;
			}
			caseCounter++;
			System.out.println("Case " + caseCounter + ": " + starsSet.size());
		}
	}
	
	public static void main(String[] args) throws Exception {
		//new Solution1().doIt(new Scanner(new BufferedInputStream(Solution1.class.getResourceAsStream("Solution1-data.txt"))));
		new Solution1().doIt(new Scanner(new BufferedInputStream(System.in)));
	}
}
