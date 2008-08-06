package com.test.image;

import java.io.File;

import com.slavi.image.DBufferedBMPImage;
import com.slavi.improc.DImageMap;
import com.slavi.util.Const;

public class TestDBufferedBMPImage {
	public static void main(String[] args) throws Exception {
		String fname = Const.smallImage;
		String fou = Const.tempDir + "/output.bmp";
		DImageMap im = new DImageMap(new File(fname));
		DBufferedBMPImage out = new DBufferedBMPImage(new File(fou), im.getSizeX(), im.getSizeY());
		try {
			for (int j = im.getSizeY() - 1; j >= 0; j--) {
				for (int i = im.getSizeX() - 1; i >= 0; i--) {
					double v = im.getPixel(i, j);
					out.setPixel(i, j, v);
				}
			}
		} finally {
			out.close();
		}
		System.out.println("DONE.");
		System.out.println("Reads  " + out.getRowReadCounter());
		System.out.println("Writes " + out.getRowWriteCounter());
	}
}
