package org.velobg.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.slavi.util.Const;

public class GenerateMapTiles {

	public static void main(String[] args) throws Exception {
		int width = 1000;
		int height = 1000;
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		
//		BufferedImage img = ImageIO.read(new File("/home/slavian/Pictures/PrintMe/Морски/P1080038.jpg"));
//		g.drawImage(img, 0, 0, null);
		int alfa = 1;
		
//		Random rnd = new Random();
		
		BasicStroke stroke = new BasicStroke(50);
		g.setStroke(stroke);
		System.out.println(System.nanoTime());
		for (int i = 0; i < 300; i++) {
			g.setColor(new Color(0, 0, 0, alfa));
//			g.drawLine(rnd.nextInt(width), rnd.nextInt(height), rnd.nextInt(width), rnd.nextInt(height));
			g.drawLine(0, 0, width, height);
		}

		int minR = 255;
		int minG = 255;
		int minB = 255;
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				int c = bi.getRGB(x, y);
				int rr = (c >> 16) & 0xff;
				int gg = (c >> 8) & 0xff;
				int bb = c & 0xff;
				
				minR = Math.min(minR, rr);
				minG = Math.min(minG, gg);
				minB = Math.min(minB, bb);
				
/*				c = c >> 16;
				c &= 0xff;
				c /= 25;
				c++;
				c *= 25;
				if (c > 255)
					c = 255;
				c = c << 16;
				bi.setRGB(x, y, c);
*/
			}
		System.out.println(minR);
		System.out.println(minG);
		System.out.println(minB);
		ImageIO.write(bi, "png", new File(Const.outputImage + ".png"));
		System.out.println(Const.outputImage + ".png");
	}
}
