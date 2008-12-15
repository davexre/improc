package com.slavi.improc.pano;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.slavi.util.Const;

public class ImagePixelInterpolation {

	public static class ImageInput {
		final BufferedImage src;
		final int width;
		final int height;
		
		public ImageInput(BufferedImage image) {
			src = image;
			width = image.getWidth();
			height = image.getHeight();
		}
		
		public int getWidth() {
			return width;
		}
		
		public int getHeight() {
			return height;
		}
		
		public int getRGBex(int x, int y) {
			if (x < 0) 
				x = 0;
			else if (x >= width) 
				x = width - 1;
			if (y < 0)
				y = 0;
			else if (y >= height)
				y = height - 1;
			return src.getRGB(x, y);
		}
		
		public int getRGB(int x, int y) {
			if ((x < 0) || (x >= width) ||
				(y < 0) || (y >= height))
				return -1;
			return src.getRGB(x, y);
		}
	}
	
	
	
	public static int getInterpolatedColor(BufferedImage ii, double x, double y) {
		int ix = (int) x;
		int iy = (int) y;
		double dx = x - ix;
		double dy = y - iy;
		
		
		
		int c = ii.getRGB(ix, iy);
		int r = (c & 0xff0000) >> 16;
		int g = (c & 0x00ff00) >> 8;
		int b = c & 0x0000ff;
		r = 0;
		
		c = (r << 16) | (g << 8) | b;
		return c;
	}	
	
	public static void main(String[] args) throws Exception {
		BufferedImage ii = ImageIO.read(new File(Const.sourceImage));
		BufferedImage oi = new BufferedImage(ii.getWidth(), ii.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int j = 0; j < ii.getHeight(); j++)
			for (int i = 0; i < ii.getWidth(); i++) {
				int c = getInterpolatedColor(ii, i, j);
				oi.setRGB(i, j, c);
			}
		ImageIO.write(oi, "bmp", new File(Const.outputImage));
	}
}
