package com.test.java;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.slavi.util.Marker;

public class LoopTestReverse {
	static String[] createArray() {
		String sArray[] = new String[1_000_000];
		for (int i = 0; i < sArray.length; i++)
			sArray[i] = "Array " + i;
		return sArray;
	}

	public static void main(String[] argv) {
		String sArray[] = createArray();
		// convert array to list
		List<String> lList = Arrays.asList(sArray);
		int iListSize = lList.size();
		// Reverse Loop Testing
		System.out.println("\n--------- Reverse Loop --------\n");
		long lReverseStartTime = System.currentTimeMillis(); // new Date().getTime();
		System.out.println("Start: " + lReverseStartTime);

		iListSize--;
		Marker.mark();
		// for loop
		//for (int i = 1000-1; i >= 0; i--)
		for (int j = 10000-1; j >= 0; j--)
		for (int k = iListSize; k >= 0; k--) {
			String temp = lList.get(k);
		}
		Marker.release();

		long lReverseEndTime = System.currentTimeMillis(); // new Date().getTime();
		System.out.println("End: " + lReverseEndTime);
		long lReverseDifference = lReverseEndTime - lReverseStartTime;
		System.out.println("For - Elapsed time in milliseconds: " + lReverseDifference);
		System.out.println("\n-------END-------");
	}
}