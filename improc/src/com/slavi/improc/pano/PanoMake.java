package com.slavi.improc.pano;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.slavi.improc.pano.Image.ImageFormat;

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
	
	public void makePano(AlignInfo ainfo) {
		for (Image image : ainfo.images) {
			
		}
	}
	
}
