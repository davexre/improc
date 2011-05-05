package com.slavi.improc.myadjust.render;

import com.slavi.image.DWindowedImageUtils;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.SafeImage;
import com.slavi.improc.myadjust.GeneratePanoramas;
import com.slavi.math.MathUtil;
import com.slavi.util.ColorConversion;

public class MaxWeightRender extends AbstractParallelRender {
	public MaxWeightRender(GeneratePanoramas parent) {
		super(parent);
	}

	double d[] = new double[3];
	double DRGB[] = new double[3];
	double HSL[] = new double[3];
	double scale = 1.0;

	public void renderRow(int row) throws Exception {
		for (int col = 0; col < outImageColor.sizeX; col++) {
			double curMaxWeight = 0.0;
			double sumWeight = 0.0;
			double sumLight = 0.0;
			double sumSaturation = 0.0;
			double curLightDiv = 0.0;
			double curSaturationDiv = 0.0;
			int color = 0;
			KeyPointList maxWeightImage = null;

			for (KeyPointList image : imageData.keySet()) {
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
				if ((ox < 0) || (ox >= image.imageSizeX) ||
					(oy < 0) || (oy >= image.imageSizeY))
					continue;
				
				int curColor = im.getRGB(ox, oy);
				if (curColor < 0)
					continue;

				double dx = Math.abs(ox - image.cameraOriginX) / image.cameraOriginX;
				double dy = Math.abs(oy - image.cameraOriginY) / image.cameraOriginY;
				double weight = 2.2 - MathUtil.hypot(dx, dy);
				
				ox /= image.pixelsPerDivision;
				oy /= image.pixelsPerDivision;
				sumWeight += weight;
				sumLight += weight * image.lightDiv[ox][oy];
				sumSaturation += weight * image.saturationDiv[ox][oy];
										
				// Calculate the color image
				if (curMaxWeight < weight) {
					maxWeightImage = image;
					curMaxWeight = weight;
					curLightDiv = image.lightDiv[ox][oy];
					curSaturationDiv = image.saturationDiv[ox][oy];
					color = curColor;
				}
			}

			int color2 = color;
			if (maxWeightImage != null) {
				ColorConversion.RGB.fromRGB(color, DRGB);
				ColorConversion.HSL.fromDRGB(DRGB, HSL);
				int sizeL = maxWeightImage.lightCDF.length - 1;
				int sizeS = maxWeightImage.saturationCDF.length - 1;
				double delta;
//				delta = maxWeightImage.saturationCDF[(int) (HSL[1] * sizeS)] - HSL[1];
//				HSL[1] += delta * scale;
				delta = maxWeightImage.lightCDF[(int) (HSL[2] * sizeL)] - HSL[2];
				HSL[2] += delta * scale;
//				HSL[2] *= (sumLight ) / (sumWeight * curLightDiv);
				ColorConversion.HSL.toDRGB(HSL, DRGB);
				color2 = ColorConversion.RGB.toRGB(DRGB);
			}
			
			outImageColor.setRGB(col, row, color);
			outImageColor2.setRGB(col, row, color2);
			if (maxWeightImage == null) {
				color = 0;
			} else {
				double h = maxWeightImage.imageId * MathUtil.C2PI * 2.0 / 13.0;
				double v = (DWindowedImageUtils.getGrayColor(color) & 0xff) / 255.0;
				double s = 1.0;
				ColorConversion.HSV.toDRGB(h, s, v, DRGB);
				color = ColorConversion.RGB.toRGB(DRGB);
			}
			outImageMask.setRGB(col, row, color);
		}
	}
}
