package com.slavi.improc.myadjust.render;

import com.slavi.image.DWindowedImageUtils;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.SafeImage;
import com.slavi.improc.myadjust.GeneratePanoramas;
import com.slavi.math.MathUtil;
import com.slavi.util.ColorConversion;

public class WeightLightRender extends AbstractParallelRender {
	public WeightLightRender(GeneratePanoramas parent) {
		super(parent);
	}

	double d[] = new double[3];
	double DRGB[] = new double[3];
	double HSL[] = new double[3];
	double scale = 1.0;

	public void renderRow(int row) throws Exception {
		for (int col = 0; col < outImageColor.imageSizeX; col++) {
			double maxWeight = 0.0;
			
			int color = 0;
			KeyPointList maxWeightImage = null;
			double maxWeightLight = 0;

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
				
				double dx = Math.abs(ox - image.cameraOriginX) / image.cameraOriginX;
				double dy = Math.abs(oy - image.cameraOriginY) / image.cameraOriginY;
				double weight = 1.5 - MathUtil.hypot(dx, dy);
				weight *= weight;
				
				int curColor = im.getRGB(ox, oy);
				if (curColor < 0)
					continue;

				// Calculate the color image
				if (maxWeight < weight) {
					maxWeightImage = image;
					ColorConversion.RGB.fromRGB(color, DRGB);
					ColorConversion.HSL.fromDRGB(DRGB, HSL);
					maxWeightLight = im.getStatLight(ox, oy);
					maxWeight = weight;
					
					color = curColor;
				}
			}

			if (maxWeightImage == null) {
				outImageColor.setRGB(col, row, 0);
				outImageColor2.setRGB(col, row, 0);
				outImageMask.setRGB(col, row, 0);
				continue;
			}

			// Calc the output image 1 color
			ColorConversion.RGB.fromRGB(color, DRGB);
			ColorConversion.HSL.fromDRGB(DRGB, HSL);
			double hue = HSL[0];
			double sumWeight = 0.0;
			double sumLight = 0.0;
			for (KeyPointList image : imageData.keySet()) {
				parent.transformWorldToCamera(col, row, image, d);
				if (d[2] < 0)
					continue;
				
				SafeImage im = imageData.get(image);
				int ox = (int)d[0];
				int oy = (int)d[1];
				
				double dx = Math.abs(ox - image.cameraOriginX) / image.cameraOriginX;
				double dy = Math.abs(oy - image.cameraOriginY) / image.cameraOriginY;
				double weight = 1.5 - MathUtil.hypot(dx, dy);
				weight *= weight;
				
				int curColor = im.getRGB(ox, oy);
				if (curColor < 0)
					continue;
				sumLight += weight * im.getStatLight(ox, oy);
				sumWeight += weight;
			}
			
			if (sumWeight == 0 || sumLight == 0.0) {
				HSL[0] = 1;
				HSL[1] = 1;
				HSL[2] = 1;
			} else 
			if ((maxWeightLight != 0.0) && (sumWeight != 0)) {
				HSL[2] *= (sumLight / sumWeight) / maxWeightLight;
			} else {
				HSL[0] = 0;
				HSL[1] = 0;
				HSL[2] = 0;
			}
				
			ColorConversion.HSL.toDRGB(HSL, DRGB);
			outImageColor.setRGB(col, row, ColorConversion.RGB.toRGB(DRGB));

			// Calc the output image 2 color
			outImageColor2.setRGB(col, row, color);
			
			// Calc the output image mask color
			double h = maxWeightImage.imageId * MathUtil.C2PI * 2.0 / 13.0;
			double v = (DWindowedImageUtils.getGrayColor(color) & 0xff) / 255.0;
			double s = 1.0;
			ColorConversion.HSV.toDRGB(h, s, v, DRGB);
			outImageMask.setRGB(col, row, ColorConversion.RGB.toRGB(DRGB));
		}
	}
}
