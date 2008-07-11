package com.slavi.parallel.img;

import java.awt.Rectangle;

import com.slavi.img.DImageMap;

public class DImageWrapper implements DWindowedImage {

	DWindowedImage imageBuf;
	DImageMap image;
	Rectangle imageExtent;
	Rectangle extent;
	
	public DImageWrapper(DImageMap image, Rectangle extent) {
		if ((image.getSizeX() < extent.x + extent.width) ||
			(image.getSizeY() < extent.y + extent.height))
			throw new IllegalArgumentException("Invalid size");
		this.imageBuf = null;
		this.image = image;
		this.imageExtent = image.getExtent();
		this.extent = extent;
	}

	public DImageWrapper(DWindowedImage image, Rectangle extent) {
		if (!image.getExtent().contains(extent))
			throw new IllegalArgumentException("Invalid size\nimage extent is " + image.getExtent() + "\nnew extent is " + extent);
		this.imageBuf = image;
		this.image = null;
		this.imageExtent = image.getExtent();
		this.extent = extent;
	}
	
	public Rectangle getExtent() {
		return new Rectangle(extent);
	}

	public double getPixel(int atX, int atY) {
		if (extent.contains(atX, atY))
			return image == null ? imageBuf.getPixel(atX, atY) : image.getPixel(atX, atY);
		else
			throw new IllegalArgumentException("Invalid coordinates X=" + 
					atX + " [" + extent.x + ".." + (extent.x + extent.width) + 
					"] Y=" + atY + " [" + extent.y + ".." + (extent.y + extent.height) + "]");
	}

	public void setPixel(int atX, int atY, double value) {
		if (extent.contains(atX, atY)) {
			if (image == null) 
				imageBuf.setPixel(atX, atY, value);
			else 
				image.setPixel(atX, atY, value);
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
