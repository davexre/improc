package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.slavi.util.file.FindFileIterator;
import com.slavi.util.swt.SwtUtil;

public class EnumerateImageFiles implements Callable<ArrayList<String>> {
	FindFileIterator imagesIterator;
	
	public EnumerateImageFiles(FindFileIterator imagesIterator) {
		this.imagesIterator = imagesIterator;
	}

	public ArrayList<String> call() throws Exception {
		ArrayList<String>result = new ArrayList<String>();
		int numberOfImages = 0;
		while (imagesIterator.hasNext()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			numberOfImages++;
			result.add(imagesIterator.next().getPath());
			SwtUtil.activeWaitDialogSetStatus("Found " + numberOfImages + " images", 0);
		}
		return result;
	}
}
