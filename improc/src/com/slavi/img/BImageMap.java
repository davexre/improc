package com.slavi.img;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class BImageMap {
	protected int sizeX;

	protected int sizeY;

	/**
	 * The pixels. Pixel values are in range -127..127 !!!
	 */
	protected byte[][] pixel;

	/**
	 * Specifies the value for the one-pixel border of the computed magnitude or
	 * direction map. Used by computeMagnitude and computeDirection.
	 */
	private static final byte borderColorValue = -127;

	public String toString() {
		int sum = 0;
		for (int i = this.sizeX - 1; i >= 0; i--)
			for (int j = this.sizeY - 1; j >= 0; j--)
				sum += this.getPixel(i, j);

		return String.format(
				"Min: " + this.min() + 
				"\nMax: " + this.max() + 
				"\nSum: " + sum + "\n");
	}

	public int getSizeX() {
		return sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public byte getPixel(int atX, int atY) {
		return pixel[atX][atY];
	}

	public void setPixel(int atX, int atY, byte aValue) {
		pixel[atX][atY] = aValue;
	}

	public BImageMap(int aSizeX, int aSizeY) {
		sizeX = 0;
		sizeY = 0;
		resize(aSizeX, aSizeY);
	}

	public BImageMap(File fImage) throws IOException {
		BufferedImage bi = ImageIO.read(fImage);
		resize(bi.getWidth(), bi.getHeight());
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				int c = bi.getRGB(i, j);
				c = (
						((c >> 16) & 0xff) + 
						((c >> 8) & 0xff) + 
						(c & 0xff)
					) / 3;
				if (c > 254) c = 254;
				pixel[i][j] = (byte) (c - 127);
			}
	}

	public void resize(int newSizeX, int newSizeY) {
		if ((newSizeX == sizeX) && (newSizeY == sizeY))
			return;
		if ((newSizeX <= 0) || (newSizeY <= 0))
			throw new Error("Invalid size parameter");
		sizeX = newSizeX;
		sizeY = newSizeY;
		pixel = new byte[sizeX][sizeY];
	}

	public void scaleHalf(BImageMap dest) {
		dest.resize(sizeX >> 1, sizeY >> 1);
		for (int i = dest.sizeX - 1; i >= 0; i--) {
			int i2 = i << 1;
			for (int j = dest.sizeY - 1; j >= 0; j--)
				dest.pixel[i][j] = pixel[i2][j << 1];
		}
	}

	public void scaleDouble(BImageMap dest) {
		dest.resize(sizeX << 1, sizeY << 1);
		for (int i = sizeX - 1; i >= 0; i--) {
			int i2 = i << 1;
			for (int j = sizeY - 1; j >= 0; j--) {
				int j2 = j << 1;
				dest.pixel[i2    ][j2    ] = 
				dest.pixel[i2 + 1][j2    ] = 
				dest.pixel[i2    ][j2 + 1] = 
				dest.pixel[i2 + 1][j2 + 1] = pixel[i][j];
			}
		}
	}

	public void computeMagnitude(BImageMap dest) {
		dest.resize(sizeX, sizeY);
		// Draw a one-pixel border. At this border magnutde CAN NOT be computed
		// adequately.
		for (int i = sizeX - 1; i >= 0; i--) {
			dest.setPixel(i, 0, borderColorValue);
			dest.setPixel(i, sizeY - 1, borderColorValue);
		}
		for (int j = sizeY - 1; j >= 0; j--) {
			dest.setPixel(0, j, borderColorValue);
			dest.setPixel(sizeX - 1, j, borderColorValue);
		}
		for (int i = sizeX - 2; i > 0; i--)
			for (int j = sizeY - 2; j > 0; j--) {
				// Magnitude is computed m = sqrt( dX ^ 2 + dY ^ 2 )
				// Since dX and dY are actually COLORS, i.e. BYTES
				// the maxumum values for dX and dY is 254, so
				// m = sqrt ( 254 ^ 2 + 254 ^ 2 ) = 359.21024...
				// ... so ...
				// Scale the magnitude to fit -127..127 interval
				// The maximum value for magnitude is 359.21024...
				dest.setPixel(i, j, (byte)(
					Math.sqrt(
						Math.pow(getPixel(i + 1, j) - getPixel(i - 1, j), 2) +
						Math.pow(getPixel(i, j + 1) - getPixel(i, j - 1), 2)
					) * (254.0 / 360.0) - 127));
			}
	}

	public void computeDirection(BImageMap dest) {
		dest.resize(sizeX, sizeY);
		// Draw a one-pixel border. At this border direction CAN NOT be computed
		// adequately.
		for (int i = sizeX - 1; i >= 0; i--) {
			dest.setPixel(i, 0, borderColorValue);
			dest.setPixel(i, sizeY - 1, borderColorValue);
		}
		for (int j = sizeY - 1; j >= 0; j--) {
			dest.setPixel(0, j, borderColorValue);
			dest.setPixel(sizeX - 1, j, borderColorValue);
		}

		for (int i = sizeX - 2; i > 0; i--)
			for (int j = sizeY - 2; j > 0; j--) {
				// Direction is computed as d = atan2( dX, dY )
				// The returned value of atan2 is from -pi to +pi.
				dest.setPixel(i, j, (byte)(
					Math.atan2(
						getPixel(i, j + 1) - getPixel(i, j - 1), 
						getPixel(i + 1, j) - getPixel(i - 1, j)
					) * (127.0 / Math.PI)));
			}
	}

	public byte max() {
		byte m = pixel[0][0];
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (m < getPixel(i, j))
					m = getPixel(i, j);
		return m;
	}

	public byte min() {
		byte m = getPixel(0, 0);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (m > getPixel(i, j))
					m = getPixel(i, j);
		return m;
	}

	public void normalize() {
		int aMin = min();
		double scale = max() - aMin;
		scale = (scale == 0.0) ? 1.0 : 254.0 / scale; 
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				setPixel(i, j, (byte)((getPixel(i, j) - aMin) * scale - 127));
	}

	public void copyTo(BImageMap dest) {
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.pixel[i][j] = pixel[i][j];
	}

	public void make0() {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				pixel[i][j] = 0;
	}

	public BufferedImage toImage() {
		int c;
		int aMin = min();
		double scale = max() - aMin;
		scale = (scale == 0.0) ? 1.0 : 255.0 / scale; 
		BufferedImage bi = new BufferedImage(sizeX, sizeY,
				BufferedImage.TYPE_INT_RGB);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				c = (int) ((getPixel(i, j) - aMin) * scale);
				bi.setRGB(i, j, (c << 16) | (c << 8) | c);
			}
		return bi;
	}
}
