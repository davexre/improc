package com.test.image;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.slavi.image.BufferedBMPImage;
import com.slavi.util.Const;

public class TestBufferedBMPImage {
	public static void main(String[] args) throws Exception {
		String fname = Const.smallImage;
		String fou = Const.tempDir + "/output.bmp";
		BufferedImage im = ImageIO.read(new File(fname));
		BufferedBMPImage out = new BufferedBMPImage(new File(fou), im.getWidth(), im.getHeight());
		try {
			for (int j = out.maxY(); j >= out.minY(); j--) 
				for (int i = out.maxX(); i >= out.minX(); i--) {
					int v = im.getRGB(i, j);
					out.setPixel(i, j, v);
				}
		} finally {
			out.close();
		}
		System.out.println("DONE.");
		System.out.println("Reads  " + out.getRowReadCounter());
		System.out.println("Writes " + out.getRowWriteCounter());
	}
}
