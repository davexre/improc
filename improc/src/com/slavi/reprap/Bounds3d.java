package com.slavi.reprap;

public class Bounds3d {
	public double minX = Double.MAX_VALUE;
	public double minY = Double.MAX_VALUE;
	public double minZ = Double.MAX_VALUE;
	
	public double maxX = Double.MIN_VALUE;
	public double maxY = Double.MIN_VALUE;
	public double maxZ = Double.MIN_VALUE;
	
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
