package com.slavi.openimaj;

import java.io.File;
import java.util.ArrayList;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

public class Test2 {

	void doIt() throws Exception {
		File imgDir = new File("/home/spetrov/.S/temp/1");
		ArrayList<MBFImage> list = new ArrayList<>();
		for (File f : imgDir.listFiles()) {
			MBFImage image = ImageUtilities.readMBF(f);
			list.add(image);
		}
		DisplayUtilities.displayLinked("title", 3, list.toArray(new MBFImage[0]));
	}

	public static void main(String[] args) throws Exception {
		new Test2().doIt();
		System.out.println("Done.");
	}
}
