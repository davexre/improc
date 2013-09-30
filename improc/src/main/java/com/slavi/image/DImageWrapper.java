package com.slavi.image;

import java.awt.Rectangle;

public class DImageWrapper implements DWindowedImage {

	DWindowedImage imageBuf;
	Rectangle extent;
	
	public DImageWrapper(DWindowedImage image, Rectangle extent) {
		if (!image.getExtent().contains(extent))
			throw new IllegalArgumentException("Invalid size\nimage extent is " + image.getExtent() + "\nnew extent is " + extent);
		this.imageBuf = image;
		this.extent = extent;
	}
	
	public Rectangle getExtent() {
		return new Rectangle(extent);
	}

	public double getPixel(int atX, int atY) {
		if (!extent.contains(atX, atY))
			throw new IllegalArgumentException("Invalid coordinates X=" + 
					atX + " [" + extent.x + ".." + (extent.x + extent.width) + 
					"] Y=" + atY + " [" + extent.y + ".." + (extent.y + extent.height) + "]");
		return imageBuf.getPixel(atX, atY);
	}

	public void setPixel(int atX, int atY, double value) {
		if (extent.contains(atX, atY)) {
			imageBuf.setPixel(atX, atY, value);
		} else
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
