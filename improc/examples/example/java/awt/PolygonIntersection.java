package example.java.awt;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import com.slavi.math.GeometryUtil;

public class PolygonIntersection {

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

		System.out.println(GeometryUtil.pathIteratorToString(intersect.getPathIterator(null)));
		System.out.println("-------");
		Area l = new Area(new Line2D.Double(0, -0.5, 2, 2));
		intersect.reset();
		intersect.add(a1);
		intersect.intersect(l);
		System.out.println(GeometryUtil.pathIteratorToString(intersect.getPathIterator(null)));
		
		System.out.println("Done.");
	}
}
