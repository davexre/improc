package com.slavi.math.transform;

public abstract class BaseTransformer<InputType, OutputType> {
	
	public abstract int getInputSize();

	public abstract int getOutputSize();
	
	public abstract double getSourceCoord(InputType item, int coordIndex);

	public abstract void setSourceCoord(InputType item, int coordIndex, double value);

	public abstract double getTargetCoord(OutputType item, int coordIndex);
	
	public abstract void setTargetCoord(OutputType item, int coordIndex, double value);
	
	public abstract void transform(InputType source, OutputType dest);

	public abstract int getNumberOfCoefsPerCoordinate();
	
	/**
	 * Transforms the source rectangle vertices into the destination 
	 * coordinate system and returns the extent of the transformed vertices.  
	 */
	public void transformExtent(InputType srcMin, InputType srcMax, OutputType destMin, OutputType destMax) {
		int inputSize = getInputSize();
		int outputSize = getOutputSize();
		
/*
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
		dest.height = maxY - minY;*/
	}
}
