package example.java.awt;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.slavi.math.MathUtil;
import com.slavi.reprap.RepRapRoutines;
import com.slavi.util.Const;

public class PolygonIntersection {
	BufferedImage bi;
	Graphics2D g;
	static final int cellOffset = 100; 
	int cumulativeCellOffsetX;
	
	Area a1;
	Area a2;
	Area a3;
	Area area;
	
	public PolygonIntersection() {
		bi = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D)bi.getGraphics();
		cumulativeCellOffsetX = 0;

		Polygon p1 = new Polygon();
		p1.addPoint(10, 10);
		p1.addPoint(90, 10);
		p1.addPoint(90, 90);
		p1.addPoint(10, 90);
		a1 = new Area(p1);
		
		Polygon p2 = new Polygon();
		p2.addPoint(20, 20);
		p2.addPoint(80, 20);
		p2.addPoint(80, 80);
		p2.addPoint(20, 80);
		a2 = new Area(p2);
		
		Polygon p3 = new Polygon();
		p3.addPoint(50, 20);
		p3.addPoint(80, 50);
		p3.addPoint(50, 80);
		p3.addPoint(20, 50);
		Area a3 = new Area(p3);
		
		area = new Area();
		area.add(a1);
		area.exclusiveOr(a2);
		area.exclusiveOr(a3);
	}

	private void nextCell() {
		g.translate(cellOffset, 0);
		cumulativeCellOffsetX += cellOffset;
		if ((cumulativeCellOffsetX + cellOffset) > bi.getWidth())
			nextRow();
	}
	
	private void nextRow() {
		g.translate(-cumulativeCellOffsetX, cellOffset);
		cumulativeCellOffsetX = 0;
	}
	
	public void save() throws IOException {
		File fou = new File(Const.tempDir, "hatch.png");
		ImageIO.write(bi, "png", fou);
		System.out.println(fou);
	}

	public void drawHatchedArea(Area a, Shape hatch) {
		g.setColor(Color.white);
		g.draw(a);
		g.fill(a);
		g.setColor(Color.red);
		g.draw(hatch);
		nextCell();
	}
	
	public void doIt() throws IOException {
		Path2D path;
		nextRow();
		for (int i = 0; i <= 16; i++) {
			path = RepRapRoutines.hatchArea(100, 100, 5, i * 22.5 * MathUtil.deg2rad, area);
			drawHatchedArea(area, path);
		}

		save();
	}

	public static void main(String[] args) throws Exception {
		new PolygonIntersection().doIt();
	}
}
