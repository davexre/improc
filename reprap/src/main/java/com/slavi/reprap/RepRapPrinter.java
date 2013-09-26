package com.slavi.reprap;

import java.awt.geom.Rectangle2D;

public interface RepRapPrinter {
	public void startPrinting(Rectangle2D bounds) throws Exception;
	
	public void printLayer(PrintLayer layer) throws Exception;
	
	public void stopPrinting() throws Exception;
}
