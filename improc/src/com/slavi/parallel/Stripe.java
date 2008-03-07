package com.slavi.parallel;

public class Stripe {

	int sizeX;
	
	int atY;
	
	double[] pixelRow;
	
	public Stripe() {
	}
	
	public Stripe(int sizeX) {
		sizeX = 0;
		resize(sizeX);
	}
	
	public void resize(int sizeX) {
		if (this.sizeX == sizeX)
			return;
		if (sizeX <= 0)
			throw new Error("Invalid size parameter");
		this.sizeX = sizeX;
		this.pixelRow = new double[sizeX];
		atY = -1;
	}
	
	public void setY(int atY) {
		this.atY = atY;
	}
	
	public int getY() {
		return atY;
	}
	
	public void initRow() {
		for (int i = pixelRow.length - 1; i >= 0; i--) {
			pixelRow[i] = 0.0;
		}
	}
	
	public double getPixel(int atX) {
		return pixelRow[atX];
	}
	
	public void setPixel(int atX, double value) {
		pixelRow[atX] = value;
	}
}
