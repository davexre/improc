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

	protected int nextColor = 0;
	
	protected BufferedImage bi;
	
	public final int sizeX, sizeY;
	
	public SafeImage(int sizeX, int sizeY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		bi = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
	}
	
	public SafeImage(InputStream image) throws IOException {
		bi = ImageIO.read(image);
		sizeX = bi.getWidth();
		sizeY = bi.getHeight();
	}
	
	public synchronized void save() throws IOException {
		String fname = Const.workDir + "/temp" + (++imageCounter) + ".png";
		ImageIO.write(bi, "png", new File(fname));
		bi = null;
	}
		
	public synchronized int getNextColor() {
		int result = colors[nextColor++];
		if (nextColor >= colors.length)
			nextColor = 0;
		return result;
	}
	
	public synchronized void setRGB(int x, int y, int rgb) {
		if (
			(x < 0) || (x >= bi.getWidth()) ||
			(y < 0) || (y >= bi.getHeight()))
			return;
		bi.setRGB(x, y, rgb);
	}
	
	public synchronized int getRGB(int x, int y) {
		if (
			(x < 0) || (x >= bi.getWidth()) ||
			(y < 0) || (y >= bi.getHeight()))
			return -1;
		return bi.getRGB(x, y) & 0x00FFFFFF;
	}

	static final int pinMarkerSize = 5;
	
	public synchronized void drawCross(int atX, int atY, int color, int color2) {
		for (int i = 0; i < pinMarkerSize; i++) {
			setRGB(atX - pinMarkerSize/2 + i, atY, color);
			setRGB(atX, atY - pinMarkerSize/2 + i, color);
		}
		if (color2 < 0)
			return;
		setRGB(atX - pinMarkerSize/2 - 1, atY, color2);
		setRGB(atX + pinMarkerSize/2 + 1, atY, color2);
		setRGB(atX, atY - pinMarkerSize/2 - 1, color2);
		setRGB(atX, atY + pinMarkerSize/2 + 1, color2);
	}
	
	public synchronized void drawX(int atX, int atY, int color, int color2) {
		for (int i = 0; i < pinMarkerSize; i++) {
			setRGB(atX - pinMarkerSize/2 + i, atY - pinMarkerSize/2 + i, color);
			setRGB(atX - pinMarkerSize/2 + i, atY + pinMarkerSize/2 - i, color);
		}		
		if (color2 < 0)
			return;
		setRGB(atX - pinMarkerSize/2 - 1, atY - pinMarkerSize/2 - 1, color2);
		setRGB(atX - pinMarkerSize/2 - 1, atY + pinMarkerSize/2 + 1, color2);
		setRGB(atX + pinMarkerSize/2 + 1, atY - pinMarkerSize/2 - 1, color2);
		setRGB(atX + pinMarkerSize/2 + 1, atY + pinMarkerSize/2 + 1, color2);
	}
	
	public synchronized void pinPair(int x1, int y1, int x2, int y2, int imgCrossColor, int imgXColor) {
		int color = getNextColor();
		drawCross(x1, y1, color, imgCrossColor);
		drawX(x2, y2, color, imgXColor);
	}
}
