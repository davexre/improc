package com.slavi.reprap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.slavi.math.MathUtil;
import com.slavi.util.Const;

public class DrawShapeOutlineTest {
	BufferedImage bi;
	Graphics2D g;
	
	Area square;
	Area triangle1;
	Area triangle2;
	Area union;
	Area xor;
	
	static final int cellOffset = 250; 
	int cumulativeCellOffsetX;
	
	public DrawShapeOutlineTest() {
		bi = new BufferedImage(1400, 1800, BufferedImage.TYPE_INT_RGB);
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
		cumulativeCellOffsetX = 0;
	}
	
	private void nextCell() {
		g.translate(cellOffset, 0);
		cumulativeCellOffsetX += cellOffset;
		if ((cumulativeCellOffsetX + cellOffset) > bi.getWidth())
			nextRow();
	}
	
	private void nextRow() {
		if (cumulativeCellOffsetX != 0) {
			g.translate(-cumulativeCellOffsetX, cellOffset);
			cumulativeCellOffsetX = 0;
		}
	}
	
	private void drawArea(Area a) {
		g.setColor(Color.blue);
		g.fill(a);
		g.setColor(Color.red);
		g.draw(a);
		nextCell();
	}
	
	private void drawHatchedArea(Area a, Path2D hatch) {
		g.setColor(Color.blue);
		g.fill(a);
		g.setColor(Color.red);
		g.draw(a);
		g.setColor(Color.white);
		g.draw(hatch);
		nextCell();
	}

	public void drawFigures() {
		drawArea(square);
		drawArea(triangle1);
		drawArea(triangle2);
		drawArea(xor);
		drawArea(union);
		nextRow();
	}
	
	public void drawPositiveOffset() {
		Stroke stroke = new BasicStroke(20);
		drawArea(RepRapRoutines.areaExpandWithBrushWidth(stroke, square));
		drawArea(RepRapRoutines.areaExpandWithBrushWidth(stroke, triangle1));
		drawArea(RepRapRoutines.areaExpandWithBrushWidth(stroke, triangle2));
		drawArea(RepRapRoutines.areaExpandWithBrushWidth(stroke, xor));
		drawArea(RepRapRoutines.areaExpandWithBrushWidth(stroke, union));
		nextRow();
	}

	public void drawNegativeOffset() {
		Stroke stroke = new BasicStroke(20);
		drawArea(RepRapRoutines.areaShrinkWithBrushWidth(stroke, square));
		drawArea(RepRapRoutines.areaShrinkWithBrushWidth(stroke, triangle1));
		drawArea(RepRapRoutines.areaShrinkWithBrushWidth(stroke, triangle2));
		drawArea(RepRapRoutines.areaShrinkWithBrushWidth(stroke, xor));
		drawArea(RepRapRoutines.areaShrinkWithBrushWidth(stroke, union));
		nextRow();
	}

	public void drawHatched() {
		Stroke stroke = new BasicStroke(20);
//		Area area = RepRapRoutines.areaShrinkWithBrushWidth(stroke, xor);
		Area area = RepRapRoutines.areaExpandWithBrushWidth(stroke, xor);
		Path2D path;
		for (int i = 0; i <= 16; i++) {
			path = RepRapRoutines.hatchArea(100, 100, 5, i * 22.5 * MathUtil.deg2rad, area);
			drawHatchedArea(area, path);
		}
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
		t.drawPositiveOffset();
		t.drawNegativeOffset();
		t.drawHatched();
		t.save();
	}
}
