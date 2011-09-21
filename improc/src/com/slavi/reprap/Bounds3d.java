package com.slavi.reprap;

public class Bounds3d {
	public double minX;
	public double minY;
	public double minZ;
	
	public double maxX;
	public double maxY;
	public double maxZ;
	
	public Bounds3d() {
		reset();
	}
	
	public void reset() {
		minX = Double.MAX_VALUE;
		minY = Double.MAX_VALUE;
		minZ = Double.MAX_VALUE;
		
		maxX = Double.MIN_VALUE;
		maxY = Double.MIN_VALUE;
		maxZ = Double.MIN_VALUE;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("min: ("); 
		result.append(minX);
		result.append(", "); 
		result.append(minY);
		result.append(", "); 
		result.append(minZ);
		result.append(") max: ("); 
		result.append(maxX);
		result.append(", "); 
		result.append(maxY);
		result.append(", "); 
		result.append(maxZ);
		result.append(")"); 
		return result.toString();
	}
}
