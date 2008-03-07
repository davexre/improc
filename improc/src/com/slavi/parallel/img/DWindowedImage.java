package com.slavi.parallel.img;

import java.awt.Rectangle;

public interface DWindowedImage {

	public Rectangle getExtent();
	
	/**
	 * Returns the minimum value for atX pixel coordinate that 
	 * can be specified in {@link #getPixel(int, int)} and
	 * {@link #setPixel(int, int, double)} 
	 * <p>
	 * Usage:<p>
	 * <pre>
	 * for (int i = dWindowedImage.minX(); i <= dWindowedImage.maxX(); i++) {
	 * //                                     ^
	 * //          note the equal sign -------|
	 *     dWindowedImage.getPixel(i, j);
	 * }
	 * <pre>
	 */
	public int minX();

	/**
	 * See {@link #minX()}
	 */
	public int maxX();

	/**
	 * See {@link #minX()}
	 */
	public int minY();
	
	/**
	 * See {@link #minX()}
	 */
	public int maxY();
	
	/**
	 * See {@link #minX()}
	 */
	public double getPixel(int atX, int atY);
	
	/**
	 * See {@link #minX()}
	 */
	public void setPixel(int atX, int atY, double value);

	/**
	 * Returns the maximum value for atX pixel coordinate that 
	 * can be specified in {@link #getPixelW(int, int)} and
	 * {@link #setPixelW(int, int, double)} 
	 * <p>
	 * Usage:<p>
	 * <pre>
	 * for (int i = 0; i <= dWindowedImage.maxXW(); i++) {
	 * //                 ^
	 * //                 |--------- note the equal sign
	 *     dWindowedImage.getPixelW(i, j);
	 * }
	 * <pre>
	 */
	public int maxXW();

	/**
	 * See {@link #maxXW()}
	 */
	public int maxYW();
	
	/**
	 * See {@link #maxXW()}
	 */
	public double getPixelW(int atX, int atY);

	/**
	 * See {@link #maxXW()}
	 */
	public void setPixelW(int atX, int atY, double value);
}
