package com.slavi.math.transform;

import java.awt.geom.Rectangle2D;

import com.slavi.math.matrix.Matrix;

public abstract class BaseTransformer {
	protected int inputSize;
	
	protected int outputSize;

	public abstract void transform(Matrix source, Matrix dest);

	public int getInputSize() {
		return inputSize;
	}

	public int getOutputSize() {
		return outputSize;
	}
	
	public abstract int getNumberOfCoefsPerCoordinate();
	
	/**
	 * Transforms the source rectangle vertices into the destination 
	 * coordinate system and returns the extent of the transformed vertices.  
	 */
	public void transformExtent(Rectangle2D.Double source, Rectangle2D.Double dest) {
		if ((inputSize != 2) || (outputSize != 2))
			throw new UnsupportedOperationException("Tranfsorm extent is 2D only operation");
		
		Matrix s = new Matrix(2, 1);
		Matrix d = new Matrix(2, 1);
		double minX, minY, maxX, maxY, t;
		
		s.setItem(0, 0, source.x);
		s.setItem(1, 0, source.y);
		transform(s, d);
		minX = maxX = d.getItem(0, 0);
		minY = maxY = d.getItem(1, 0);

		s.setItem(0, 0, source.x + source.width);
		s.setItem(1, 0, source.y);
		transform(s, d);
		t = d.getItem(0, 0);
		if (minX > t) minX = t;
		if (maxX < t) maxX = t;
		t = d.getItem(1, 0);
		if (minY > t) minY = t;
		if (maxY < t) maxY = t;
		
		s.setItem(0, 0, source.x);
		s.setItem(1, 0, source.y + source.height);
		transform(s, d);
		t = d.getItem(0, 0);
		if (minX > t) minX = t;
		if (maxX < t) maxX = t;
		t = d.getItem(1, 0);
		if (minY > t) minY = t;
		if (maxY < t) maxY = t;

		s.setItem(0, 0, source.x + source.width);
		s.setItem(1, 0, source.y + source.height);
		transform(s, d);
		t = d.getItem(0, 0);
		if (minX > t) minX = t;
		if (maxX < t) maxX = t;
		t = d.getItem(1, 0);
		if (minY > t) minY = t;
		if (maxY < t) maxY = t;
		
		dest.x = minX;
		dest.y = minY;
		dest.width = maxX - minX;
		dest.height = maxY - minY;
	}
}
