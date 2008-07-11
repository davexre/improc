package com.slavi.parallel.img;

import java.awt.Rectangle;

/**
 * This class represents a gray scale image with the pixels stored 
 * in an array of doubles used by routines for parallel image processing.
 * The origin of the buffer MAY be translated to a specified origin. 
 * By default all pixel values are in the range [0..1].
 */
public class PDImageMapBuffer implements DWindowedImage {

	int sizeX; 
	int sizeY;
	double pixels[][];
	Rectangle extent;
	
	public PDImageMapBuffer(Rectangle extent) {
		this.extent = new Rectangle(extent);
		this.sizeX = extent.width;
		this.sizeY = extent.height;
		pixels = new double[extent.width][extent.height];
	}
	
	public Rectangle getExtent() {
		return new Rectangle(extent);
	}

	public void setExtent(Rectangle extent) {
		if ((extent.width <= sizeX) && (extent.height <= sizeY)) 
			this.extent = extent;
		else
			throw new IllegalArgumentException("Invalid extent");
	}
	
	public double getPixel(int atX, int atY) {
		if (extent.contains(atX, atY))
			return pixels[atX - extent.x][atY - extent.y];
		else
			throw new IllegalArgumentException("Invalid coordinates X=" + 
					atX + " [" + extent.x + ".." + (extent.x + extent.width) + 
					"] Y=" + atY + " [" + extent.y + ".." + (extent.y + extent.height) + "]");
	}

	public void setPixel(int atX, int atY, double value) {
		if (extent.contains(atX, atY))
			pixels[atX - extent.x][atY - extent.y] = value;
		else
			throw new IllegalArgumentException("Invalid coordinates X=" + 
					atX + " [" + extent.x + ".." + (extent.x + extent.width) + 
					"] Y=" + atY + " [" + extent.y + ".." + (extent.y + extent.height) + "]");
	}

	public int maxX() {
		return extent.x + extent.width - 1;
	}

	public int maxY() {
		return extent.y + extent.height - 1;
	}

	public int minX() {
		return extent.x;
	}

	public int minY() {
		return extent.y;
	}
}
