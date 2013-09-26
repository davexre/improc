package com.slavi.reprap;

import java.awt.geom.Area;
import java.awt.geom.Path2D;

public class PrintLayer extends Layer {
	public Area infills;
	public Path2D infillsHatch;
	
	public Area outfills;
	public Path2D outfillsHatch;
	
	public Path2D supportHatch;
	
	public Area outline;
		
	public PrintLayer(Layer copyFrom) {
		super(copyFrom);
	}
}
