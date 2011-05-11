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
		for (int col = 0; col < outImageColor.sizeX; col++) {
			double maxWeight = 0.0;
			
			double sumWeight = 0.0;
			double sumHue = 0.0;
			double sumSaturation = 0.0;
			double sumLight = 0.0;

			double maxSumWeight = 0.0;
			double maxSumHue = 0.0;
			double maxSumSaturation = 0.0;
			double maxSumLight = 0.0;
			
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
				
				double dx = Math.abs(ox - image.cameraOriginX) / image.cameraOriginX;
				double dy = Math.abs(oy - image.cameraOriginY) / image.cameraOriginY;
				double weight = 1.5 - MathUtil.hypot(dx, dy);
				weight *= weight;
				
				// Calculate the average HSL in a region around the current point
				int radius = 1;
				double curSumWeight = 0.0;
				double curSumHue = 0.0;
				double curSumSaturation = 0.0;
				double curSumLight = 0.0;
				
				for (int i = -radius; i <= radius; i++) {
					for (int j = -radius; j <= radius; j++) {
						int tmpColor = im.getRGB(ox + i, oy + j);
						if (tmpColor < 0)
							continue;
						ColorConversion.RGB.fromRGB(tmpColor, DRGB);
						ColorConversion.HSL.fromDRGB(DRGB, HSL);
						
						curSumWeight += weight;
						curSumHue += weight * HSL[0];
						curSumSaturation += weight * HSL[1];
						curSumLight += weight * HSL[2];
					}					
				}
				sumWeight += curSumWeight;
				sumHue += curSumHue;
				sumSaturation += curSumSaturation;
				sumLight += curSumLight;

				int curColor = im.getRGB(ox, oy);
				if (curColor < 0)
					continue;

				// Calculate the color image
				if (maxWeight < weight) {
					maxWeightImage = image;
					maxWeight = weight;
					
					maxSumWeight = curSumWeight;
					maxSumHue = curSumHue;
					maxSumSaturation = curSumSaturation;
					maxSumLight = curSumLight;

					color = curColor;
				}
			}

			if (maxWeightImage == null) {
				outImageColor.setRGB(col, row, 0);
				outImageColor2.setRGB(col, row, 0);
				outImageMask.setRGB(col, row, 0);
				continue;
			}

			maxSumHue /= maxSumWeight;
			maxSumSaturation /= maxSumWeight;
			maxSumLight /= maxSumWeight;
			
			sumHue /= sumWeight;
			sumSaturation /= sumWeight;
			sumLight /= sumWeight;

			// Calc the output image 1 color
			ColorConversion.RGB.fromRGB(color, DRGB);
			ColorConversion.HSL.fromDRGB(DRGB, HSL);
//			if (maxSumHue != 0) {
//				HSL[0] *= sumHue / maxSumHue;
//			}
//			if (maxSumSaturation != 0) {
//				HSL[1] *= sumSaturation / maxSumSaturation;
//			}
			if (maxSumLight != 0) {
				HSL[2] *= sumLight / maxSumLight;
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
