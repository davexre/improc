package com.slavi.reprap;

import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Locale;

import com.slavi.util.Const;

public class RepRapGCodePrinter implements RepRapPrinter {
	
	File fou;
	PrintWriter out;
	
	public RepRapGCodePrinter() throws FileNotFoundException {
		fou = new File(Const.tempDir, "reprap.out");
	}
	
	public void startPrinting(Rectangle2D bounds) throws Exception {
		out = new PrintWriter(fou);
		out.println("M110");	// Reset the line numbers
		out.println("G21");		// metric is good!
		out.println("G90");		// absolute positioning
		out.println("M140 S65.0");	// set bed temperature and return
		out.println("M113");	// set extruder to use pot for PWM
		out.println("M109 S205.0");	// set temperature and wait
	}

	boolean valveOpen = false;
	
	private void horizontalMove(double x, double y, boolean valveOpen) {
		if (this.valveOpen != valveOpen) {
			this.valveOpen = valveOpen;
			if (valveOpen)
				out.println("M126 P1");	// valve open
			else
				out.println("M127 P1");	// valve closed
		}
		out.format(Locale.US, "G1 X%1.1f Y%1.1f\n", x, y); // horizontal move
	}
	
	private void printPath(PathIterator iter) {
		double coords[] = new double[6];
		double lastMoveToX = 0;
		double lastMoveToY = 0;
		
		while (!iter.isDone()) {
			int seg = iter.currentSegment(coords);
			switch (seg) {
			case PathIterator.SEG_MOVETO:
				lastMoveToX = coords[0];
				lastMoveToY = coords[1];
				horizontalMove(coords[0], coords[1], false);
				break;
			case PathIterator.SEG_LINETO:
				horizontalMove(coords[0], coords[1], true);
				break;
			case PathIterator.SEG_CLOSE:
				horizontalMove(lastMoveToX, lastMoveToY, true);
				break;
			case PathIterator.SEG_QUADTO:
			case PathIterator.SEG_CUBICTO:
			default:
				break;
			}
			iter.next();
		}
	}
	
	public static void main(String[] args) {
		double asd = 123.456;
		System.out.format(Locale.US, "asd %1.2f qwe\n", asd);
	}
	
	public void printLayer(PrintLayer layer) throws Exception {
		out.println("T0");		// select new extruder
		out.println("G28 X0");	// set x 0
		out.println("G28 Y0");	// set y 0
		out.println("G92 E0");	// zero the extruded length
		
		printPath(layer.outline.getPathIterator(null));
		printPath(layer.infillsHatch.getPathIterator(null));
		printPath(layer.outfillsHatch.getPathIterator(null));
//		printPath(layer.supportHatch.getPathIterator(null));
	}
	
	public void stopPrinting() throws Exception {
		out.println("M0");		//shut RepRap down
		out.close();
		out = null;
		System.out.println(fou);
	}
}
