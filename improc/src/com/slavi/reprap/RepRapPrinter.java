package com.slavi.reprap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;

import javax.imageio.ImageIO;

import com.slavi.util.Const;
import com.slavi.util.file.FileUtil;

public class RepRapPrinter {
	
	File outputDir;
	BufferedImage bi;
	Graphics2D g;
	
	public RepRapPrinter() {
		outputDir = new File(Const.tempDir, "layers");
	}
	
	public void startPrinting(Bounds3d bounds) throws Exception {
		FileUtil.removeDirectory(outputDir);
		outputDir.mkdirs();

		bi = new BufferedImage(1200, 1200,
//				(int)((bounds.maxX - bounds.minX) + 1), 
//				(int)((bounds.maxY - bounds.minY) + 1),
				BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) bi.getGraphics();
	}
	
	public void printLayer(PrintLayer layer) throws Exception {
		g.setColor(Color.black);
		g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		
//		g.setColor(Color.blue);
//		g.fill(layer.slice);
		g.setColor(Color.red);
		g.draw(layer.slice);

		g.setColor(Color.yellow);
		g.draw(layer.infillsHatch);
		g.setColor(Color.green);
		g.draw(layer.outfillsHatch);
		g.setColor(Color.blue);
		g.draw(layer.supportHatch);
//		g.setColor(Color.white);
//		g.draw(layer.outline);
					
		File fou = new File(outputDir, String.format(Locale.US, "layer_%04d.png", layer.layerNumber));
		System.out.println(fou + " " + layer.z);
		ImageIO.write(bi, "png", fou);
	}
	
	public void stopPrinting() throws Exception {
		g = null;
		bi = null;
	}
}
