package com.slavi.parallel.img;

import java.awt.Rectangle;

import com.slavi.img.DImageMap;

public class DImageWrapper implements DWindowedImage {

	DImageMap image;
	Rectangle imageExtent;
	Rectangle extent;
	
	public DImageWrapper(DImageMap image, Rectangle extent) {
		if ((image.getSizeX() < extent.x + extent.width) ||
			(image.getSizeY() < extent.y + extent.height))
			throw new Error("Invalid size");
		this.image = image;
		this.imageExtent = image.getExtent();
		this.extent = extent;
	}
	
	public Rectangle getExtent() {
		return new Rectangle(extent);
	}

	public double getPixel(int atX, int atY) {
		if (extent.contains(atX, atY))
			return image.getPixel(atX, atY);
		else
			throw new Error("Invalid coordinates");
	}

	public void setPixel(int atX, int atY, double value) {
		if (extent.contains(atX, atY))
			image.setPixel(atX, atY, value);
		else
			throw new Error("Invalid coordinates");
	}

	public double getPixelW(int atX, int atY) {
		return getPixelW(extent.x + atX, extent.y + atY);
	}

	public void setPixelW(int atX, int atY, double value) {
		setPixelW(extent.x + atX, extent.y + atY, value);
	}

	public int maxX() {
		return extent.x + extent.width - 1;
	}

	public int maxXW() {
		return extent.x - 1;
	}

	public int maxY() {
		return extent.y + extent.height - 1;
	}

	public int maxYW() {
		return extent.y - 1;
	}

	public int minX() {
		return extent.x;
	}

	public int minY() {
		return extent.y;
	}
}
