package example.java.awt;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

public class PolygonIntersection {

	public static void printPathIterator(PathIterator iter) {
		double coords[] = new double[6];
		while (!iter.isDone()) {
			int seg = iter.currentSegment(coords);
			String type;
			switch (seg) {
			case PathIterator.SEG_MOVETO:
				type = "MOVETO";
				break;
			case PathIterator.SEG_LINETO:
				type = "LINETO";
				break;
			case PathIterator.SEG_QUADTO:
				type = "QUADTO";
				break;
			case PathIterator.SEG_CUBICTO:
				type = "CUBICTO";
				break;
			case PathIterator.SEG_CLOSE:
				type = "CLOSE";
				break;
			default:
				type = "<N/A>";
				break;
			}
			System.out.println(type + "\t" + coords[0] + "\t" + coords[1]);
			iter.next();
		}
	}
	
	public static void main(String[] args) {
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(0, 0);
		p1.lineTo(1, 0);
		p1.lineTo(0, 1);
		p1.closePath();
		
		Polygon p2 = new Polygon();
		p2.addPoint(0, 0);
		p2.addPoint(1, 0);
		p2.addPoint(1, 1);
		
//		p2.translate(10, 10);
		Area a1 = new Area(p1);
		Area a2 = new Area(p2);
		Area intersect = new Area();
		intersect.add(a1);
		intersect.add(a2);
		
		printPathIterator(intersect.getPathIterator(null));
		System.out.println("Done.");
	}
}
