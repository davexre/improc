package com.slavi.improc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.slavi.util.Const;

public class SafeImage {

	static int imageCounter = 0;
	
	static final int colors[] = {
		0x00ff0000,
		0x0000ff00,
		0x000000ff,		
		0x0000ffff,		
		0x00ff00ff,		
		0x00ffff00,
		0x00ff9600
	};

	int nextColor = 0;
	
	public BufferedImage bi;
	
	public SafeImage(int sizeX, int sizeY) {
		bi = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
	}
	
	public SafeImage(InputStream image) throws IOException {
		bi = ImageIO.read(image);
	}
	
	public void save() throws IOException {
		String fname = Const.tempDir + "/temp" + (++imageCounter) + ".png";
		ImageIO.write(bi, "png", new File(fname));
		bi = null;
	}
		
	public int getNextColor() {
		int result = colors[nextColor++];
		if (nextColor >= colors.length)
			nextColor = 0;
		return result;
	}
	
	public void setRGB(int x, int y, int rgb) {
		if (
			(x < 0) || (x >= bi.getWidth()) ||
			(y < 0) || (y >= bi.getHeight()))
			return;
		bi.setRGB(x, y, rgb);
	}
	
	public int getRGB(int x, int y) {
		if (
			(x < 0) || (x >= bi.getWidth()) ||
			(y < 0) || (y >= bi.getHeight()))
			return -1;
		return bi.getRGB(x, y) & 0x00FFFFFF;
	}

	public void drawCross(int atX, int atY, int color) {
		for (int i = 0; i < 5; i++) {
			setRGB(atX - 2 + i, atY, color);
			setRGB(atX, atY - 2 + i, color);
		}
	}
	
	public void drawX(int atX, int atY, int color) {
		for (int i = 0; i < 5; i++) {
			setRGB(atX - 2 + i, atY - 2 + i, color);
			setRGB(atX - 2 + i, atY + 2 - i, color);
		}		
	}
	
	public void pinPair(int x1, int y1, int x2, int y2) {
		int color = getNextColor();
		drawCross(x1, y1, color);
		drawX(x2, y2, color);
	}
}
