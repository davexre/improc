package com.slavi.parallel.img;

import java.awt.image.BufferedImage;

/**
 * This class represents a gray scale image with the pixels stored 
 * in an array of doubles used by routines for parallel image processing. 
 * By default all pixel values are in the range [0..1].
 */
public class PDImageMap {
	/**
	 * See #getSizeX()
	 */
	int sizeX;

	/**
	 * See #getSizeY() 
	 */
	int sizeY;
	
	int originX;
	int originY;
	
	int windowX;
	int windowY;
	
	/**
	 * The pixels of the image. The first index of the array is 
	 * x (width) and the second index is the y (height).
	 */
	double pixel[][];

	/**
	 * Width of the image in pixels.
	 */
	public int getSizeX() {
		return sizeX;
	}

	/**
	 * Height of the image in pixels.
	 */
	public int getSizeY() {
		return sizeY;
	}

	/**
	 * Origin of the image window.
	 */
	public int getOriginX() {
		return originX;
	}

	/**
	 * Origin of the image window.
	 */
	public int getOriginY() {
		return originY;
	}

	/**
	 * Returns a pixel from the image.
	 */
	public double getPixel(int atX, int atY) {
		return pixel[originX + atX][originY + atY];
	}

	/**
	 * Width of the image window in pixels.
	 */
	public int getWindowX() {
		return windowX;
	}

	/**
	 * Height of the image window in pixels.
	 */
	public int getWindowY() {
		return windowY;
	}

	/**
	 * Set the value of a pixel with indexes according to window origin
	 */
	public void setPixelWin(int atX, int atY, double aValue) {
		pixel[atX][atY] = aValue;
	}

	/**
	 * Returns a pixel from the image with indexes according to window origin.
	 */
	public double getPixelWin(int atX, int atY) {
		return pixel[atX][atY];
	}

	/**
	 * Set the value of a pixel
	 */
	public void setPixel(int atX, int atY, double aValue) {
		pixel[originX + atX][originY + atY] = aValue;
	}

	public PDImageMap(BufferedImage image, int originX, int originY, int windowX, int windowY) {
		resize(image.getWidth(), image.getHeight(), originX, originY, windowX, windowY);
		for (int i = windowX - 1, ix = originX + windowX - 1; i >= 0; i--, ix--)
			for (int j = windowY - 1, jy = originY + windowY - 1; j >= 0; j--, jy--) {
				int c = image.getRGB(ix, jy);
				c = ((c >> 16) & 0xff) + 
					((c >> 8) & 0xff) + 
					(c & 0xff);
				setPixelWin(i, j, c / (3.0 * 255.0));
			}
	}
	
	public void resize(int sizeX, int sizeY, int originX, int originY, int windowX, int windowY) {
		if ((originX + windowX > sizeX) ||
			(originY + windowY > sizeY) ||
			(sizeX <= 0) ||
			(sizeY <= 0) ||
			(originX < 0) ||
			(originY < 0) ||
			(windowX <= 0) ||
			(windowY <= 0))
			throw new Error("Invalid size parameter");
		
		this.originX = originX;
		this.originY = originY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		
		if ((this.windowX == windowX) &&
			(this.windowY == windowY))
			return;
		
		this.windowX = windowX;
		this.windowY = windowY;
		this.pixel = new double[windowX][windowY];
	}
}
