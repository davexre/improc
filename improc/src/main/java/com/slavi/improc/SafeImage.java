package com.slavi.improc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import com.slavi.util.ColorConversion;
import com.slavi.util.Const;

public class SafeImage {

	static final AtomicInteger imageSaveCounter = new AtomicInteger(0);
	
	public static final int colors[] = {
		0x00ff0000,
		0x0000ff00,
		0x000000ff,		
		0x0000ffff,		
		0x00ff00ff,		
		0x00ffff00,
		0x00ff9600
	};

	protected int nextColor = 0;
	
	public BufferedImage bi;
	
	public final int imageSizeX, imageSizeY;
	
	public SafeImage(int imageSizeX, int imageSizeY) {
		this.imageSizeX = imageSizeX;
		this.imageSizeY = imageSizeY;
		bi = new BufferedImage(imageSizeX, imageSizeY, BufferedImage.TYPE_INT_RGB);
	}
	
	public SafeImage(InputStream image) throws IOException {
		bi = ImageIO.read(image);
		imageSizeX = bi.getWidth();
		imageSizeY = bi.getHeight();
	}
	
	public synchronized void save(String fname) throws IOException {
		ImageIO.write(bi, "png", new File(fname));
		bi = null;
	}
	
	public synchronized void save() throws IOException {
		String fname = Const.workDir + "/temp" + imageSaveCounter.incrementAndGet() + ".png";
		save(fname);
	}
		
	public synchronized int getNextColor() {
		int result = colors[nextColor++];
		if (nextColor >= colors.length)
			nextColor = 0;
		return result;
	}
	
	public synchronized void setRGB(int x, int y, int rgb) {
		if (
			(x < 0) || (x >= imageSizeX) ||
			(y < 0) || (y >= imageSizeY))
			return;
		bi.setRGB(x, y, rgb);
	}
	
	public synchronized int getRGB(int x, int y) {
		if (
			(x < 0) || (x >= imageSizeX) ||
			(y < 0) || (y >= imageSizeY))
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
	
	///////////////////////////////////
	// Image statistics
	///////////////////////////////////
	
	static final int divisions = 30;
	int divSizeInPixels;
	int divSizeX, divSizeY; 

	double lightStat[];
	
	static final int numHueDivisions = 3;
	double lightByHue[];
	
	private int getArrayIndex(int imgX, int imgY) {
		if (imgX < 0) imgX = 0;
		if (imgY < 0) imgY = 0;
		
		if (imgX >= imageSizeX) imgX = imageSizeX - 1;
		if (imgY >= imageSizeY) imgY = imageSizeY - 1;

		return (imgX / divSizeInPixels) + (imgY / divSizeInPixels) * divSizeX;
	}
	
	private int getHueIndex(double hue) {
		int hueIndex = (int) (0.5 + hue * numHueDivisions * 2.0 / Math.PI);
		if (hueIndex < 0) hueIndex = 0;
		if (hueIndex >= numHueDivisions) hueIndex = numHueDivisions - 1;
		return hueIndex;
	}
	
	public double getStatLightByHue(double hue, int imgX, int imgY) {
		return lightByHue[getArrayIndex(imgX, imgY) * getHueIndex(hue)];
	}
	
	public double getStatLight(int imgX, int imgY) {
		return lightStat[getArrayIndex(imgX, imgY)];
	}

	public void populateStatistics() throws IOException {
		int maxSize = Math.max(imageSizeX, imageSizeY);
		divSizeInPixels = Math.max(100, maxSize / divisions);
		divSizeX = (int) Math.ceil((double)imageSizeX / divSizeInPixels);
		divSizeY = (int) Math.ceil((double)imageSizeY / divSizeInPixels);

		int arraySize = divSizeX * divSizeY;
		lightStat = new double[arraySize];
		lightByHue = new double[arraySize * numHueDivisions];
		
		double DRGB[] = new double[3];
		double HSL[] = new double[3];
		double hueLight[] = new double[numHueDivisions];
		int hueLightCount[] = new int[numHueDivisions];
		
		for (int divY = 0; divY < divSizeY; divY++) {
			int startY = divY * divSizeInPixels;
			int stopY = Math.min(imageSizeY, startY + divSizeInPixels); 
			for (int divX = 0; divX < divSizeX; divX++) {
				int startX = divX * divSizeInPixels;
				int stopX = Math.min(imageSizeX, startX + divSizeInPixels);
				
				// Start computing new division
				int pixelsCount = 0;
				double sumLight = 0;
				Arrays.fill(hueLight, 0);
				Arrays.fill(hueLightCount, 0);
				
				for (int imgY = startY; imgY < stopY; imgY++) {
					for (int imgX = startX; imgX < stopX; imgX++) {
						pixelsCount++;
						int color = bi.getRGB(imgX, imgY);
						ColorConversion.RGB.fromRGB(color, DRGB);
						ColorConversion.HSL.fromDRGB(DRGB, HSL);
						sumLight += HSL[2];
						
						int hueIndex = getHueIndex(HSL[0]);
						hueLight[hueIndex] += HSL[0];
						hueLightCount[hueIndex]++;
					}
				}
				
				// Set the computed division value
				int arrayIndex = divX + divY * divSizeX;
				lightStat[arrayIndex] = sumLight / pixelsCount;
				for (int hueIndex = 0; hueIndex < numHueDivisions; hueIndex++) {
					int count = hueLightCount[hueIndex];
					lightByHue[arrayIndex * hueIndex] = count == 0 ? 0 : hueLight[hueIndex] / count; 
				}
			}
		}
		/*
		BufferedImage bo = new BufferedImage(imageSizeX, imageSizeY, BufferedImage.TYPE_INT_RGB);
		
		for (int j = 0; j < imageSizeY; j++) {
			for (int i = 0; i < imageSizeX; i++) {
				double v = getStatLight(i, j);
				int color = (int) (v * 255.0 + 0.5);
				color &= 255;
				color = color << 16 | color << 8 | color;
				bo.setRGB(i, j, color);
			}			
		}
		
		String fname = Const.workDir + "/temp" + imageSaveCounter.incrementAndGet() + ".png";
		ImageIO.write(bo, "png", new File(fname));*/
	}
}
