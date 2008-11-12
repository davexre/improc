package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.slavi.util.Const;
import com.slavi.util.file.FindFileIterator;
import com.slavi.util.ui.SwtUtil;

public class Dummy {
	
	public static class Dummmmmmy implements Callable<ArrayList<String>> {

		public ArrayList<String> call() throws Exception {
			Thread.sleep(2000);
			return null;
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		FindFileIterator imagesIterator = FindFileIterator.makeWithWildcard(Const.imagesDir + "/*.jpg", true, true);
		ArrayList<String> res = SwtUtil.openWaitDialog("Searching for images", new Dummmmmmy(), -1); // new EnumerateImageFiles(imagesIterator), -1);
		for (String s : res)
			System.out.println(s);
		System.out.println(res.size());
	}
}
