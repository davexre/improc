package com.slavi.improc.ui;

import java.util.ArrayList;

import com.slavi.util.Const;
import com.slavi.util.file.FindFileIterator;
import com.slavi.util.ui.SwtUtl;

public class Dummy {
	public static void main(String[] args) throws Exception {
		FindFileIterator imagesIterator = FindFileIterator.makeWithWildcard(Const.imagesDir + "/*.jpg", true, true);
		ArrayList<String> res = SwtUtl.openWaitDialog("Searching for images", new EnumerateImageFiles(imagesIterator), -1);
		for (String s : res)
			System.out.println(s);
		System.out.println(res.size());
	}
}
