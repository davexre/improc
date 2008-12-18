package com.slavi.improc.pano;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.slavi.improc.pano.Image.ImageFormat;
import com.slavi.util.Const;

public class PanoMake {

	static final double MAX_FISHEYE_FOV = 160.0;
	
	void checkMakeParams(AlignInfo ainfo) {
		if ((ainfo.pano.format == ImageFormat.Rectilinear) &&
			(ainfo.pano.hfov >= 180.0))
			throw new RuntimeException("Rectilinear Panorama can not have 180 or more degrees field of view.");
		for (Image im : ainfo.images) {
			if ((im.format == ImageFormat.Rectilinear) &&
				(im.hfov >= 180.0))
				throw new RuntimeException("Rectilinear Image can not have 180 or more degrees field of view.");
			if ((im.format == ImageFormat.FisheyeCirc || im.format == ImageFormat.FisheyeFF) &&
				(im.hfov > MAX_FISHEYE_FOV))
				throw new RuntimeException("Fisheye lens processing limited to fov <= " + MAX_FISHEYE_FOV);
		}
	}
	
	boolean isColorSpecifiec(Image im) {
		if (im.cP.radial) {
			for (int i = 0; i < 4; i++)
				if ((im.cP.radial_params[0][i] != im.cP.radial_params[1][i]) ||
					(im.cP.radial_params[2][i] != im.cP.radial_params[1][i]))
					return true;
		}
		if (im.cP.vertical) {
			if ((im.cP.vertical_params[0] != im.cP.vertical_params[1]) ||
				(im.cP.vertical_params[2] != im.cP.vertical_params[1]))
				return true;
		}
		if (im.cP.horizontal) {
			if ((im.cP.horizontal_params[0] != im.cP.horizontal_params[1]) ||
				(im.cP.horizontal_params[2] != im.cP.horizontal_params[1]))
				return true;
		}
		return false;
	}
		
	public void makePanoOneImage(Image image, Image pano, int colorIndex) throws IOException {
		double w2 = pano.width / 2.0 - 0.5;
		double h2 = pano.height / 2.0 - 0.5;
		double sw2 = image.width / 2.0 - 0.5;
		double sh2 = image.height / 2.0 - 0.5;
		Point2D.Double p = new Point2D.Double();
		BufferedImage bimage = ImageIO.read(new File(image.name));
		
		for (int y = pano.height - 1; y >= 0; y--) {
			double y_d = (double) y - h2;
			
			for (int x = pano.width - 1; x >= 0; x--) {
				double x_d = (double) x - w2;
				// Get source Cartesian coordinates 
				p.x = x_d;
				p.y = y_d;
				PanoAdjust.makeParams(p, image, pano, colorIndex);
				// Convert source Cartesian coordinates to screen coordinates
				p.x += sw2;
				p.y += sh2;
				// Is the pixel valid, i.e. from within source image?
				if ((p.x < 0) || (p.x >= image.width) ||
					(p.y < 0) || (p.y >= image.height))
					continue;
				
				
				
				// Extract integer and fractions of source screen coordinates
				int xc = (int) p.x;
				int yc = (int) p.y;
				p.x -= xc;
				p.y -= yc;
				
				int color = bimage.getRGB(xc, yc);
			}
		}
	}
	
	public static void makePano(AlignInfo ainfo) throws IOException {
		BufferedImage bi = new BufferedImage(ainfo.pano.width, ainfo.pano.height, BufferedImage.TYPE_INT_RGB);
		Point2D.Double p = new Point2D.Double();
		Point2D.Double p2 = new Point2D.Double();
		
		double srcX = ainfo.pano.width / 2.0 - 0.5;
		double srcY = ainfo.pano.height / 2.0 - 0.5;

		double scale = ainfo.pano.width / ainfo.pano.extentInPano.width;
		
		for (Image image : ainfo.images) {
			int x1 = (int) image.extentInPano.x;
			int x2 = (int) (image.extentInPano.width + image.extentInPano.x);
			int y1 = (int) image.extentInPano.y;
			int y2 = (int) (image.extentInPano.height + image.extentInPano.y);
			BufferedImage im = ImageIO.read(new File(image.name));

			double destX = image.width / 2.0 - 0.5;
			double destY = image.height / 2.0 - 0.5;
			
			for (int j = y1; j <= y2; j++)
				for (int i = x1; i <= x2; i++) {
					int atX = (int) ((i - ainfo.pano.extentInPano.x) * scale);
					int atY = (int) ((j - ainfo.pano.extentInPano.y) * scale);

					if ((atX < 0) || (atX >= ainfo.pano.width) ||
						(atY < 0) || (atY >= ainfo.pano.height))
							continue;
					
					p.x = atX - srcX; 
					p.y = atY - srcY;
					PanoAdjust.makeParams(p, image, ainfo.pano, 0);
//					p2.x = p.x;
//					p2.y = p.y;
//					PanoAdjust.makeInvParams(p, image, ainfo.pano, 0);
//					p2.x += srcX;
//					p2.y += srcY;
					
					p.x += destX;
					p.y += destY;
					
					int x = (int) p.x;
					int y = (int) p.y;
					if ((x < 0) || (x >= im.getWidth()) ||
						(y < 0) || (y >= im.getHeight()))
						continue;
					bi.setRGB(atX, atY, im.getRGB(x, y));
				}
		}
		System.out.println("Output file is " + Const.outputImage);
		ImageIO.write(bi, "jpg", new File(Const.outputImage));
	}
	
}
