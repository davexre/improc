package com.test.java;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.slavi.util.Marker;

public class LoopTestForward {
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

		// Forward Loop Testing
		System.out.println("\n--------- Forward Loop --------\n");
		long lForwardStartTime = System.currentTimeMillis(); // new Date().getTime();
		System.out.println("Start: " + lForwardStartTime);

		Marker.mark();
		// for loop
//		for (int i = 0; i < iListSize; i++)
		for (int j = 0; j < 10000; j++)
		for (int k = 0; k < iListSize; k++) {
			String temp = lList.get(k);
		}
		Marker.release();
		
		long lForwardEndTime = System.currentTimeMillis(); // new Date().getTime();
		System.out.println("End: " + lForwardEndTime);
		long lForwardDifference = lForwardEndTime - lForwardStartTime;
		System.out.println("Forward Looping - Elapsed time in milliseconds: " + lForwardDifference);
		System.out.println("\n-------END-------");
	}
}