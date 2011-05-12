package com.slavi.improc.myadjust.render;

import com.slavi.image.DWindowedImageUtils;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.SafeImage;
import com.slavi.improc.myadjust.GeneratePanoramas;
import com.slavi.math.MathUtil;

public class WeightMergeRender extends AbstractParallelRender {
	public WeightMergeRender(GeneratePanoramas parent) {
		super(parent);
	}

	private int fixColorValue(long color, long count) {
		if (count == 0)
			return 0;
		color /= count;
		if (color <= 0)
			return 0;
		if (color >= 255)
			return 255;
		return (int) color;
	}
	
	double d[] = new double[3];
	public void renderRow(int row) throws Exception {
		for (int col = 0; col < outImageColor.imageSizeX; col++) {
			long colorR = 0;
			long colorG = 0;
			long colorB = 0;

			long mcolorR = 0;
			long mcolorG = 0;
			long mcolorB = 0;
			
			int countR = 0;
			int countG = 0;
			int countB = 0;

			int mcountR = 0;
			int mcountG = 0;
			int mcountB = 0;

			int curMaxColor = 0;

			int index = 0;
			for (KeyPointList image : imageData.keySet()) {
				index++;
				if (Thread.currentThread().isInterrupted())
					throw new InterruptedException();
				
				if (
					(image.min.x > col) || 
					(image.min.y > row) || 
					(image.max.x < col) || 
					(image.max.y < row))
					continue;
				
				parent.transformWorldToCamera(col, row, image, d);
				if (d[2] < 0)
					continue;

				SafeImage im = imageData.get(image);
				int ox = (int)d[0];
				int oy = (int)d[1];
				int color = im.getRGB(ox, oy);
				if (color < 0)
					continue;

				double precision = 1000.0;
				double dx = Math.abs(ox - image.cameraOriginX) / image.cameraOriginX;
				double dy = Math.abs(oy - image.cameraOriginY) / image.cameraOriginY;
				int weight = 1 + (int) (precision * (2 - MathUtil.hypot(dx, dy)));  
				
				// Calculate the masked image
				int grayColor = DWindowedImageUtils.getGrayColor(color) & 0xff;
				switch (index % 3) {
				case 0:
					mcolorR += grayColor;
					mcountR += weight;
					break;
				case 1:
					mcolorG += grayColor;
					mcountG += weight;
					break;
				default:
					mcolorB += grayColor;
				mcountB += weight;
				break;
				}
				
				// Calculate the color image
				countR += weight;
				countG += weight;
				countB += weight;
				colorR += weight * ((color >> 16) & 0xff);
				colorG += weight * ((color >> 8) & 0xff);
				colorB += weight * (color & 0xff);
			}

			int color = 
				(fixColorValue(colorR, countR) << 16) |
				(fixColorValue(colorG, countG) << 8) |
				fixColorValue(colorB, countB);
			outImageColor.setRGB(col, row, color);
			color = 
				(fixColorValue(mcolorR, mcountR) << 16) |
				(fixColorValue(mcolorG, mcountG) << 8) |
				fixColorValue(mcolorB, mcountB);
			outImageMask.setRGB(col, row, curMaxColor);
		}
	}
}
