package com.test.image;

import java.io.File;

import com.slavi.image.DWindowedBMPImage;
import com.slavi.image.DWindowedImage;
import com.slavi.image.PDImageMapBuffer;
import com.slavi.util.Const;

public class TestDBufferedBMPImage {
	public static void main(String[] args) throws Exception {
		String fname = Const.smallImage;
		String fou = Const.tempDir + "/output.bmp";
		DWindowedImage im = new PDImageMapBuffer(new File(fname));
		DWindowedBMPImage out = DWindowedBMPImage.create(new File(fou), im.maxX() + 1, im.maxY() + 1);
		try {
			for (int j = im.maxY(); j >= im.minX(); j--) {
				for (int i = im.maxX(); i >= im.minY(); i--) {
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
