package com.slavi.reprap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.slavi.util.Const;

public class DrawShapeOutlineTest {
	BufferedImage bi;
	Graphics2D g;
	
	Area square;
	Area triangle1;
	Area triangle2;
	Area union;
	Area xor;
	
	static final int offset = 250; 
	int offsetX;
	
	public DrawShapeOutlineTest() {
		bi = new BufferedImage(1400, 1600, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D)bi.getGraphics();
		
		square = new Area(new Rectangle(-100, -100, 200, 200));
		triangle1 = new Area(new Polygon(
			new int[] {-100, 100, 0}, 
			new int[] {-50, -50, 50},
			3));
		triangle2 = new Area(new Polygon(
			new int[] {-100, 100, 0}, 
			new int[] {50, 50, -50},
			3));

		union = new Area();
		union.add(square);
		union.add(triangle1);
		union.add(triangle2);

		xor = new Area();
		xor.exclusiveOr(square);
		xor.exclusiveOr(triangle1);
		xor.exclusiveOr(triangle2);

		g.translate(120, 120);
		offsetX = 0;
	}
	
	private void nextCell() {
		g.translate(offset, 0);
		offsetX += offset;
	}
	
	private void nextRow() {
		g.translate(-offsetX, offset);
		offsetX = 0;
	}
	
	public void drawFigures() {
		g.setColor(Color.white);
		g.draw(square);

		nextCell();
		g.setColor(Color.green);
		g.draw(triangle1);

		nextCell();
		g.setColor(Color.green);
		g.draw(triangle2);
		
		nextCell();
		g.setColor(Color.blue);
		g.fill(xor);

		nextCell();
		g.setColor(Color.white);
		g.fill(union);
		
		nextRow();
	}
	
	public void drawPositiveOffset() {
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(20));

		g.setColor(Color.white);
		g.fill(square);
		g.draw(square);

		nextCell();
		g.setColor(Color.green);
		g.fill(triangle1);
		g.draw(triangle1);
		
		nextCell();
		g.setColor(Color.green);
		g.fill(triangle2);
		g.draw(triangle2);
		
		nextCell();
		g.setColor(Color.blue);
		g.fill(xor);
		g.draw(xor);
		
		nextCell();
		g.setColor(Color.white);
		g.fill(union);
		g.draw(union);

		nextRow();
		g.setStroke(oldStroke);
	}
	
	public void drawNegativeOffset() {
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(20));

		g.setColor(Color.white);
		g.fill(square);
		g.setColor(Color.black);
		g.draw(square);
		
		nextCell();
		g.setColor(Color.green);
		g.fill(triangle1);
		g.setColor(Color.black);
		g.draw(triangle1);
		
		nextCell();
		g.setColor(Color.green);
		g.fill(triangle2);
		g.setColor(Color.black);
		g.draw(triangle2);
		
		nextCell();
		g.setColor(Color.blue);
		g.fill(xor);
		g.setColor(Color.black);
		g.draw(xor);
		
		nextCell();
		g.setColor(Color.white);
		g.fill(union);
		g.setColor(Color.black);
		g.draw(union);
		
		nextRow();
		g.setStroke(oldStroke);
	}
	
	private Area positiveOffset(Stroke stroke, Area area) {
		Area result = new Area();
		result.add(area);
		result.add(new Area(stroke.createStrokedShape(area)));
		return result;
	}
	
	public void drawPositiveOffset2() {
		Stroke stroke = new BasicStroke(20);

		g.setColor(Color.white);
		g.fill(positiveOffset(stroke, square));
		
		nextCell();
		g.setColor(Color.green);
		g.fill(positiveOffset(stroke, triangle1));
		
		nextCell();
		g.setColor(Color.green);
		g.fill(positiveOffset(stroke, triangle2));
		
		nextCell();
		g.setColor(Color.blue);
		g.fill(positiveOffset(stroke, xor));
		
		nextCell();
		g.setColor(Color.white);
		g.fill(positiveOffset(stroke, union));
		
		nextRow();
	}

	private Area negativeOffset(Stroke stroke, Area area) {
		Area result = new Area();
		result.add(area);
		result.subtract(new Area(stroke.createStrokedShape(area)));
		return result;
	}
	
	public void drawNegativeOffset2() {
		Stroke stroke = new BasicStroke(20);

		g.setColor(Color.white);
		g.fill(negativeOffset(stroke, square));

		nextCell();
		g.setColor(Color.green);
		g.fill(negativeOffset(stroke, triangle1));
		
		nextCell();
		g.setColor(Color.green);
		g.fill(negativeOffset(stroke, triangle2));
		
		nextCell();
		g.setColor(Color.blue);
		g.fill(negativeOffset(stroke, xor));
		
		nextCell();
		g.setColor(Color.white);
		g.fill(negativeOffset(stroke, union));
		
		nextRow();
	}

	public void drawHatched() {
		Area area = square;
		g.setColor(Color.white);
		Line2D.Double l = new Line2D.Double();
		Rectangle2D r = area.getBounds2D();
		l.setLine(r.getMinX(), r.getMinY(), r.getMaxX(), r.getMaxY());
		
		Area tmp = new Area(l);
		g.fill(tmp);
		
		nextRow();
	}
	
	public void save() throws IOException {
		File fou = new File(Const.tempDir, "output.png");
		ImageIO.write(bi, "png", fou);
		System.out.println(fou);
	}
	
	public static void main(String[] args) throws IOException {
		DrawShapeOutlineTest t = new DrawShapeOutlineTest();
		t.drawFigures();
//		t.drawPositiveOffset();
//		t.drawNegativeOffset();
		t.drawPositiveOffset2();
		t.drawNegativeOffset2();
		t.drawHatched();
		t.save();
	}
}
