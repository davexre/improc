package com.slavi.parallel.img;

import java.awt.Rectangle;

public class ImageWriteTracker implements DWindowedImage {

	boolean traceReads, traceWrites;
	
	DWindowedImage tracedImage;
	
	public final PDImageMapBuffer mask;
	
	public ImageWriteTracker(DWindowedImage tracedImage, boolean traceReads, boolean traceWrites) {
		this.tracedImage = tracedImage;
		this.traceReads = traceReads;
		this.traceWrites = traceWrites;
		this.mask = new PDImageMapBuffer(tracedImage.getExtent());
		DWindowedImageUtils.make0(this.mask);
	}
	
	public Rectangle getExtent() {
		return tracedImage.getExtent();
	}

	public double getPixel(int atX, int atY) {
		if (traceReads) 
			mask.setPixel(atX, atY, mask.getPixel(atX, atY) + 1);
		return tracedImage.getPixel(atX, atY);
	}

	public int maxX() {
		return tracedImage.maxX();
	}

	public int maxY() {
		return tracedImage.maxY();
	}

	public int minX() {
		return tracedImage.minX();
	}

	public int minY() {
		return tracedImage.minY();
	}

	public void setPixel(int atX, int atY, double value) {
		if (traceWrites) 
			mask.setPixel(atX, atY, mask.getPixel(atX, atY) + 1);
		tracedImage.setPixel(atX, atY, value);
	}
}
