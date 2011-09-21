package com.slavi.reprap;

import java.awt.geom.Area;

public class Layer {
	public double objectZ;
	public int layerNumber;
	public double reprapZ;
	
	public Area slice;
	public Area support;
	
	public Layer() {
	}
	
	public Layer(Layer copyFrom) {
		objectZ = copyFrom.objectZ;
		layerNumber = copyFrom.layerNumber;
		reprapZ = copyFrom.reprapZ;
		slice = copyFrom.slice;
		support = copyFrom.support;
	}
}
