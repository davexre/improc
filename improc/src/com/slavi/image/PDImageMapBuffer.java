package com.slavi.image;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

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
		sizeX = extent.width;
		sizeY = extent.height;
		pixels = new double[sizeX][sizeY];
	}
	
	/**
	 * Reads the image and converts it to gray scale values in the
	 * range [0..1].
	 */
	public PDImageMapBuffer(BufferedImage image) {
		sizeX = image.getWidth(); 
		sizeY = image.getHeight();
		extent = new Rectangle(sizeX, sizeY);
		pixels = new double[sizeX][sizeY];

		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				int c = image.getRGB(i, j);
				pixels[i][j] = (
						((c >> 16) & 0xff) + 
						((c >> 8) & 0xff) + 
						 (c & 0xff)) / (3.0 * 255.0);
			}
	}
	
	/**
	 * Reads the image and converts it to gray scale values in the
	 * range [0..1].
	 */
	public PDImageMapBuffer(File image) throws IOException {
		this(ImageIO.read(image));
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
		if (!extent.contains(atX, atY))
			throw new IllegalArgumentException("Invalid coordinates X=" + 
					atX + " [" + extent.x + ".." + (extent.x + extent.width) + 
					"] Y=" + atY + " [" + extent.y + ".." + (extent.y + extent.height) + "]");
		return pixels[atX - extent.x][atY - extent.y];
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
