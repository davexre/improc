package com.slavi.reprap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.slavi.util.Const;

public class DrawShapeTest {

	static final int offset = 250; 
	
	public static void main1(String[] args) throws IOException {
		BufferedImage bi = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D)bi.getGraphics();
		g.setColor(Color.white);

		Path2D p1 = new Path2D.Double();
		p1.moveTo(100, 100);
		p1.lineTo(200, 100);
		p1.lineTo(100, 200);
		p1.closePath();
		
		Polygon p2 = new Polygon();
		p2.addPoint(100, 100);
		p2.addPoint(200, 100);
		p2.addPoint(200, 200);

		Area a1 = new Area(p1);
		Area a2 = new Area(p2);
		Area union = new Area();
		union.add(a1);
		union.add(a2);

		Area intersect = new Area();
		intersect.add(a1);
		intersect.intersect(a2);
				
		g.setColor(Color.white);
		g.fill(union);
		g.setColor(Color.blue);
		g.fill(intersect);
		g.setColor(Color.red);
		g.draw(a1);
		g.translate(100, 100);
		g.setColor(Color.green);
		g.draw(a2);
		
		File fou = new File(Const.tempDir, "output.png");
		ImageIO.write(bi, "png", fou);
		System.out.println(fou);
	}
	
	public static void main2(String[] args) throws IOException {
		BufferedImage bi = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D)bi.getGraphics();
		g.setColor(Color.white);

		Area a1 = new Area(new Rectangle(0, 0, 100, 100));
		Area a2 = new Area(new Ellipse2D.Double(0, 25, 100, 50));
		Area union = new Area();
		union.add(a1);
		union.add(a2);

		Area intersect = new Area();
		intersect.add(a1);
		intersect.exclusiveOr(a2);

		g.translate(0, 100);

		g.setColor(Color.red);
		g.draw(a1);

		g.translate(110, 0);
		g.setColor(Color.green);
		g.draw(a2);

		g.translate(110, 0);
		g.setColor(Color.blue);
		g.fill(intersect);

		g.translate(110, 0);
		g.setColor(Color.white);
		g.fill(union);
		
		
		File fou = new File(Const.tempDir, "output.png");
		ImageIO.write(bi, "png", fou);
		System.out.println(fou);
	}
	
	public static void main3(String[] args) throws IOException {
		BufferedImage bi = new BufferedImage(1200, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D)bi.getGraphics();
		g.setColor(Color.white);

		Area square = new Area(new Rectangle(-100, -100, 200, 200));
		Area triangle1 = new Area(new Polygon(
				new int[] {-100, 100, 0}, 
				new int[] {-50, -50, 50},
				3));
		Area triangle2 = new Area(new Polygon(
				new int[] {-100, 100, 0}, 
				new int[] {50, 50, -50},
				3));

		Area union = new Area();
		union.add(square);
		union.add(triangle1);
		union.add(triangle2);

		Area xor = new Area();
		xor.exclusiveOr(square);
		xor.exclusiveOr(triangle1);
		xor.exclusiveOr(triangle2);

		g.translate(100, 100);

		g.setColor(Color.red);
		g.draw(square);

		g.translate(offset, 0);
		g.setColor(Color.green);
		g.draw(triangle1);

		g.translate(offset, 0);
		g.setColor(Color.green);
		g.draw(triangle2);
		
		g.translate(offset, 0);
		g.setColor(Color.blue);
		g.fill(xor);

		g.translate(offset, 0);
		g.setColor(Color.white);
		g.fill(union);
		
		
		File fou = new File(Const.tempDir, "output.png");
		ImageIO.write(bi, "png", fou);
		System.out.println(fou);
	}
	
	public static void main(String[] args) throws IOException {
		// outline
		BufferedImage bi = new BufferedImage(1400, 1600, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D)bi.getGraphics();

		Area square = new Area(new Rectangle(-100, -100, 200, 200));
		Area triangle1 = new Area(new Polygon(
				new int[] {-100, 100, 0}, 
				new int[] {-50, -50, 50},
				3));
		Area triangle2 = new Area(new Polygon(
				new int[] {-100, 100, 0}, 
				new int[] {50, 50, -50},
				3));

		Area union = new Area();
		union.add(square);
		union.add(triangle1);
		union.add(triangle2);

		Area xor = new Area();
		xor.exclusiveOr(square);
		xor.exclusiveOr(triangle1);
		xor.exclusiveOr(triangle2);

		g.translate(120, 120);

		g.setColor(Color.white);
		g.draw(square);

		g.translate(offset, 0);
		g.setColor(Color.green);
		g.draw(triangle1);

		g.translate(offset, 0);
		g.setColor(Color.green);
		g.draw(triangle2);
		
		g.translate(offset, 0);
		g.setColor(Color.blue);
		g.fill(xor);

		g.translate(offset, 0);
		g.setColor(Color.white);
		g.fill(union);
		
		// Outline positive
		g.setStroke(new BasicStroke(20));

		g.translate(0, offset);

		g.setColor(Color.white);
		g.fill(union);
		g.draw(union);
		
		g.translate(-offset, 0);
		g.setColor(Color.blue);
		g.fill(xor);
		g.draw(xor);
		
		g.translate(-offset, 0);
		g.setColor(Color.green);
		g.fill(triangle2);
		g.draw(triangle2);
		
		g.translate(-offset, 0);
		g.setColor(Color.green);
		g.fill(triangle1);
		g.draw(triangle1);
		
		g.translate(-offset, 0);
		g.setColor(Color.white);
		g.fill(square);
		g.draw(square);
	
		// Outline negative
		g.setStroke(new BasicStroke(20));

		g.translate(0, offset);

		g.setColor(Color.white);
		g.fill(square);
		g.setColor(Color.black);
		g.draw(square);
		
		g.translate(offset, 0);
		g.setColor(Color.green);
		g.fill(triangle1);
		g.setColor(Color.black);
		g.draw(triangle1);
		
		g.translate(offset, 0);
		g.setColor(Color.green);
		g.fill(triangle2);
		g.setColor(Color.black);
		g.draw(triangle2);
		
		g.translate(offset, 0);
		g.setColor(Color.blue);
		g.fill(xor);
		g.setColor(Color.black);
		g.draw(xor);
		
		g.translate(offset, 0);
		g.setColor(Color.white);
		g.fill(union);
		g.setColor(Color.black);
		g.draw(union);

		// Positive offset 2
		g.setStroke(new BasicStroke());
		Stroke stroke = new BasicStroke(20);
		g.translate(0, offset);

		Area sUnion = new Area(stroke.createStrokedShape(union));
		g.setColor(Color.white);
		g.fill(sUnion);
		
		Area sXor = new Area(stroke.createStrokedShape(xor));
		g.translate(-offset, 0);
		g.setColor(Color.blue);
		g.fill(sXor);
		
		Area sTriangle2 = new Area(stroke.createStrokedShape(triangle2));
		g.translate(-offset, 0);
		g.setColor(Color.green);
		g.fill(sTriangle2);
		
		Area sTriangle1 = new Area(stroke.createStrokedShape(triangle1));
		g.translate(-offset, 0);
		g.setColor(Color.green);
		g.fill(sTriangle1);
		
		Area sSquare = new Area(stroke.createStrokedShape(square));
		g.translate(-offset, 0);
		g.setColor(Color.white);
		g.fill(sSquare);
		
		// Outline negative
		g.translate(0, offset);

		g.setColor(Color.white);
		square.subtract(sSquare);
		g.fill(square);
		
		g.translate(offset, 0);
		g.setColor(Color.green);
		triangle1.subtract(sTriangle1);
		g.fill(triangle1);
		
		g.translate(offset, 0);
		g.setColor(Color.green);
		triangle2.subtract(sTriangle2);
		g.fill(triangle2);
		
		g.translate(offset, 0);
		g.setColor(Color.blue);
		xor.subtract(sXor);
		g.fill(xor);
		
		g.translate(offset, 0);
		g.setColor(Color.white);
		union.subtract(sUnion);
		g.fill(union);
		
		File fou = new File(Const.tempDir, "output.png");
		ImageIO.write(bi, "png", fou);
		System.out.println(fou);
	}
	
}
