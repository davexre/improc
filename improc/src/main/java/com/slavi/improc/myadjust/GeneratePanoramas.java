package com.slavi.improc.myadjust;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.SafeImage;
import com.slavi.improc.myadjust.render.WeightLightRender;
import com.slavi.improc.myadjust.render.WeightMergeRender;
import com.slavi.math.MathUtil;
import com.slavi.util.ColorConversion;
import com.slavi.util.Marker;
import com.slavi.util.concurrent.TaskSetExecutor;
import com.slavi.util.ui.SwtUtil;

public class GeneratePanoramas implements Callable<Void> {

	static boolean validateTransformerInCalcExtent = true;
	
	ExecutorService exec;
	PanoTransformer panoTransformer;
	ArrayList<ArrayList<KeyPointPairList>> panos;
	
	ArrayList<KeyPointList> images;
	ArrayList<KeyPointPairList> pairLists;

	///////
	
	public AtomicInteger rowsProcessed;
	public Map<KeyPointList, SafeImage> imageData = new HashMap<KeyPointList, SafeImage>();
	public SafeImage outImageColor;
	public SafeImage outImageColor2;
	public SafeImage outImageMask;
	
	/* 
	 * Parameters for Helmert transformation from PanoTransformer 
	 * coordinate system into output image coordinate system.
	 */ 
	Point2D.Double panoOrigin = new Point2D.Double();
	Point2D.Double panoSize = new Point2D.Double();
	int outputImageSizeX = 2000;
	int outputImageSizeY;

	final String outputDir;
	final boolean pinPoints;
	final boolean useColorMasks;
	final boolean useImageMaxWeight;

	public GeneratePanoramas(ExecutorService exec,
			String panoTransformerClassName,
			ArrayList<ArrayList<KeyPointPairList>> panos,
			String outputDir,
			boolean pinPoints,
			boolean useColorMasks,
			boolean useImageMaxWeight) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.exec = exec;
		this.panoTransformer = (PanoTransformer) Class.forName(panoTransformerClassName).newInstance();
		this.panos = panos;
		this.outputDir = outputDir;
		this.pinPoints = pinPoints;
		this.useColorMasks = useColorMasks;
		this.useImageMaxWeight = useImageMaxWeight;
	}
	
	static void minMax(double rx, double ry, Point2D.Double min, Point2D.Double max) {
		if (rx < min.x) 
			min.x = rx;
		if (ry < min.y) 
			min.y = ry;
		if (rx > max.x) 
			max.x = rx;
		if (ry > max.y) 
			max.y = ry;
	}

	public void transformWorldToCamera(double x, double y, KeyPointList image, double dest[]) {
		x = panoSize.x * (x / outputImageSizeX) + panoOrigin.x;
		y = panoSize.y * (y / outputImageSizeY) + panoOrigin.y;
		panoTransformer.transformBackward(x, y, image, dest);
	}
	
	void transformCameraToWorld(double x, double y, KeyPointList image, double dest[]) {
		panoTransformer.transformForeward(x, y, image, dest);
		dest[0] = outputImageSizeX * ((dest[0] - panoOrigin.x) / panoSize.x);
		dest[1] = outputImageSizeY * ((dest[1] - panoOrigin.y) / panoSize.y);
	}

	/**
	 * Calculates the extent of all images, and determines the output image size.
	 */
	void calcExtents() throws InterruptedException {
		int step = 20;
		double dest[] = new double[3];
		double tmp[] = new double[3];
		
		for (KeyPointList image : images) {
			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();
			image.min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			image.max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
			for (int i = 0; i < image.imageSizeX; i+=step) {
				for (int j = 0; j < image.imageSizeY; j+=step) {
					panoTransformer.transformForeward(i, j, image, dest);
					if (validateTransformerInCalcExtent) {
						panoTransformer.transformBackward(dest[0], dest[1], image, tmp);
						if ((Math.abs(tmp[0] - i) > 0.5) || (Math.abs(tmp[1] - j) > 0.5))
							throw new RuntimeException("Error validating transformer i=" + i + " j=" + j + " backI=" + tmp[0] + " backJ=" + tmp[1]);
					}
					minMax(dest[0], dest[1], image.min, image.max);
				}
				panoTransformer.transformForeward(i, image.imageSizeY - 1, image, dest);
				minMax(dest[0], dest[1], image.min, image.max);
			}
			for (int j = 0; j < image.imageSizeY; j+=step) {
				panoTransformer.transformForeward(image.imageSizeX - 1, j, image, dest);
				minMax(dest[0], dest[1], image.min, image.max);
			}
			panoTransformer.transformForeward(image.imageSizeX - 1, image.imageSizeY - 1, image, dest);
			minMax(dest[0], dest[1], image.min, image.max);
		}

		panoOrigin.x = Double.POSITIVE_INFINITY;
		panoOrigin.y = Double.POSITIVE_INFINITY;
		panoSize.x = Double.NEGATIVE_INFINITY;
		panoSize.y = Double.NEGATIVE_INFINITY;
		for (KeyPointList image : images) {
			minMax(image.min.x, image.min.y, panoOrigin, panoSize);
			minMax(image.max.x, image.max.y, panoOrigin, panoSize);
		}
		panoSize.x -= panoOrigin.x;
		panoSize.y -= panoOrigin.y;
		
		outputImageSizeY = (int)(outputImageSizeX * (panoSize.y / panoSize.x));
		
		for (KeyPointList image : images) {
			image.min.x = outputImageSizeX * ((image.min.x - panoOrigin.x) / panoSize.x);
			image.min.y = outputImageSizeY * ((image.min.y - panoOrigin.y) / panoSize.y);
			image.max.x = outputImageSizeX * ((image.max.x - panoOrigin.x) / panoSize.x);
			image.max.y = outputImageSizeY * ((image.max.y - panoOrigin.y) / panoSize.y);
		}
	}

	private void pinPointPairs(SafeImage oi) throws InterruptedException {
		double d[] = new double[3];
		double DRGB[] = new double[3];
		for (KeyPointList image : images) {
			double h = image.imageId * MathUtil.C2PI * 2.0 / 13.0;
			ColorConversion.HSV.toDRGB(h, 1.0, 1.0, DRGB);
			int color = ColorConversion.RGB.toRGB(DRGB);
			for (int i = 0; i < image.imageSizeX; i++) {
				transformCameraToWorld(i, 0, image, d);
				oi.setRGB((int) d[0], (int) d[1], color);
				transformCameraToWorld(i, image.imageSizeY - 1, image, d);
				oi.setRGB((int) d[0], (int) d[1], color);
			}
			
			for (int j = 0; j < image.imageSizeY; j++) {
				transformCameraToWorld(0, j, image, d);
				oi.setRGB((int) d[0], (int) d[1], color);
				transformCameraToWorld(image.imageSizeX - 1, j, image, d);
				oi.setRGB((int) d[0], (int) d[1], color);
			}
		}
		
		for (KeyPointPairList pairList : pairLists) {
			int colorCross;
			int colorX;
			
			if (useImageMaxWeight) {
				double h = pairList.source.imageId * MathUtil.C2PI * 2.0 / 13.0;
				ColorConversion.HSV.toDRGB(h, 1.0, 1.0, DRGB);
				colorCross = ColorConversion.RGB.toRGB(DRGB);

				h = pairList.target.imageId * MathUtil.C2PI * 2.0 / 13.0;
				ColorConversion.HSV.toDRGB(h, 1.0, 1.0, DRGB);
				colorX = ColorConversion.RGB.toRGB(DRGB);
			} else {
				colorCross = images.indexOf(pairList.source);;
				colorCross = colorCross % 3;
				colorCross = colorCross < 0 ? -1 : 255 << (8 * colorCross);
				
				colorX = images.indexOf(pairList.target);
				colorX = colorX % 3;
				colorX = colorX < 0 ? -1 : 255 << (8 * colorX);			
			}

			for (KeyPointPair pair : pairList.items) {
				if (!pair.panoBad) {
					transformCameraToWorld(pair.sourceSP.getDoubleX(), pair.sourceSP.getDoubleY(), pairList.source, d);
					int x1 = (int)d[0];
					int y1 = (int)d[1];
					transformCameraToWorld(pair.targetSP.getDoubleX(), pair.targetSP.getDoubleY(), pairList.target, d);
					int x2 = (int)d[0];
					int y2 = (int)d[1];
					oi.pinPair(x1, y1, x2, y2, colorCross, colorX);
				}
			}
		}
	}
	
/*	
	private void drawWorldMesh(SafeImage img) {
		int cols[] = {
			// 0		15			30		45			60		75
			0xff0000, 0xffffff, 0xffffff, 0xffffff, 0xffffff, 0xffffff, 
			// 90		105			120		135			150		165
			0x00ff00, 0xffffff, 0xffffff, 0xffffff, 0xffffff, 0xffffff,
			// 180		195			210		225			240		255
			0x0000ff, 0xffffff, 0xffffff, 0xffffff, 0xffffff, 0xffffff,
			// 270		285			300		315			330		345		360
			0xffff00, 0xffffff, 0xffffff, 0xffffff, 0xffffff, 0xffffff
		};
		int numDivisionsX = cols.length;
		// draw meridians
		for (int i = numDivisionsX - 1; i >= 0; i--) {
			int colorX = cols[i]; // i == 0 ? 0xff0000 : 0xffffff;
			double xd = i * 2 * Math.PI / numDivisionsX - MathUtil.PIover2;
			int x = (int) (outputImageSizeX * ((xd - panoOrigin.x) / panoSize.x));
			for (int j = outputImageSizeY - 1; j >= 0; j--) {
				img.setRGB(outputImageSizeX - 1 - x, j, colorX);
			}
		}
		// draw parallels
		int y = (int) (outputImageSizeY * ((-panoOrigin.y) / panoSize.y));
		for (int i = img.sizeX - 1; i >= 0; i--) {
			img.setRGB(outputImageSizeX - 1 - i, y, 0x00ff00);
		}
	}

	void dumpPointData(String outputFile) throws Exception {
		PrintStream out = new PrintStream(outputFile);
		
		out.println("Image generated from the following images:");
		for (KeyPointList image : images) {
			out.println(image.imageFileStamp.getFile().getName() + "\t(" + image.items.size() + ")");
		}
		out.println("------------");
		
		for (KeyPointPairList pairList : pairLists) {
			out.println(
				pairList.source.imageFileStamp.getFile().getName() + "\t" +
				pairList.target.imageFileStamp.getFile().getName() + "\t" +
				pairList.items.size());
		}
		out.println("------------");

		out.println("minAngle.x=" + MathUtil.rad2degStr(panoOrigin.x));
		out.println("minAngle.y=" + MathUtil.rad2degStr(panoOrigin.y));
		out.println("sizeAngle.x=" + MathUtil.rad2degStr(panoSize.x));
		out.println("sizeAngle.y=" + MathUtil.rad2degStr(panoSize.y));
		out.println("outputImageSizeX=" + outputImageSizeX);
		out.println("outputImageSizeY=" + outputImageSizeY);
		for (KeyPointList image : images) {
			out.println(image.imageFileStamp.getFile().getName() +
					"\tmin.x=" + MathUtil.rad2degStr(image.min.x) + 
					"\tmin.y=" + MathUtil.rad2degStr(image.min.y) + 
					"\tmax.x=" + MathUtil.rad2degStr(image.max.x) + 
					"\tmax.y=" + MathUtil.rad2degStr(image.max.y) + 
					"\tcameraOriginX=" + MathUtil.d4(image.cameraOriginX) + 
					"\tcameraOriginY=" + MathUtil.d4(image.cameraOriginY) + 
					"\tcameraScale=" + MathUtil.d4(image.cameraScale) + 
					"\timageSizeX=" + image.imageSizeX + 
					"\timageSizeY=" + image.imageSizeY + 
					"\trx=" + MathUtil.rad2degStr(image.rx) + 
					"\try=" + MathUtil.rad2degStr(image.ry) + 
					"\trz=" + MathUtil.rad2degStr(image.rz) + 
					"\tscaleZ=" + MathUtil.d4(image.scaleZ)
					);
			
		}
		out.println();
		out.println("------------");

		out.println(
				"Source\t" +
				"Target\t" +
				"Bad\t" +
				"PanoBad\t" +
				"Discrepancy\t" +
				"Distance1\t" +
				"Distance2\t" +
				
				"SdogLevel\t" +
				"SimgScale\t" +
				"SkpScale\t" +
				"SadjS\t" + 

				"TdogLevel\t" +
				"TimgScale\t" +
				"TkpScale\t" +
				"TadjS");
		for (KeyPointPairList pairList : pairLists) {
			for (KeyPointPair pair : pairList.items) {
				out.println(
					pair.sourceSP.keyPointList.imageFileStamp.getFile().getName() + "\t" +
					pair.targetSP.keyPointList.imageFileStamp.getFile().getName() + "\t" +
					pair.bad + "\t" +
					pair.panoBad + "\t" +
					MathUtil.d4(pair.discrepancy) + "\t" +
					MathUtil.d4(pair.distanceToNearest) + "\t" +
					MathUtil.d4(pair.distanceToNearest2) + "\t" +
					
					pair.sourceSP.dogLevel + "\t" +
					pair.sourceSP.imgScale + "\t" +
					pair.sourceSP.kpScale + "\t" +
					pair.sourceSP.adjS + "\t" +
					
					pair.targetSP.dogLevel + "\t" +
					pair.targetSP.imgScale + "\t" +
					pair.targetSP.kpScale + "\t" +
					pair.targetSP.adjS);
			}
		}
		out.close();
	}
*/	
	
	private void labelImageNames(SafeImage oi) {
		double d[] = new double[3];
		double DRGB[] = new double[3];
		Graphics2D gr = (Graphics2D) oi.bi.getGraphics();
		int fontSize = (int) Math.max(outputImageSizeY / 90, 10);
		Font font = new Font("Arial", Font.BOLD, fontSize);
		gr.setFont(font);
		Color colorRect = Color.black;
		FontMetrics fm = gr.getFontMetrics();
		int fontDescent = fm.getDescent();
		int fontHeight = fm.getAscent() + fontDescent;
		for (KeyPointList image : images) {
			double h = image.imageId * MathUtil.C2PI * 2.0 / 13.0;
			ColorConversion.HSV.toDRGB(h, 1.0, 1.0, DRGB);
			int color = ColorConversion.RGB.toRGB(DRGB);

			String str = image.imageFileStamp.getFile().getName();
			Rectangle2D rect= fm.getStringBounds(str, gr);
			transformCameraToWorld(image.cameraOriginX, image.cameraOriginY, image, d);

			double scale = 1.2;
			double width = Math.max(4, rect.getWidth()) * scale;
			double height = Math.max(4, fontHeight) * scale;
			double atX = d[0] - width / 2.0;
			double atY = d[1] - height / 2.0;
			gr.setColor(colorRect);
			gr.fillRect((int) atX, (int) atY, (int) width, (int) height);
			gr.setColor(new Color(color));
			gr.drawRect((int) atX, (int) atY, (int) width, (int) height);
			
			atX = d[0] - rect.getWidth() / 2.0;
			atY = d[1] + fontHeight / 2.0 - fontDescent - fm.getLeading() - 1;
			gr.drawString(str, (int) atX, (int) atY);
		}
	}
/*	
	double lightCDF[] = new double[KeyPointList.histogramSize];
	double saturationCDF[] = new double[KeyPointList.histogramSize];
	void calcHistograms() {
		double sum = 0.0;
		for (KeyPointList image : images) {
			sum += image.imageSizeX * image.imageSizeY;
		}
		// Compute pano histograms
		Arrays.fill(lightCDF, 0);
		Arrays.fill(saturationCDF, 0);
		for (KeyPointList image : images) {
			double imageWeight = image.imageSizeX * image.imageSizeY / sum;
			for (int i = 0; i < lightCDF.length; i++) {
				lightCDF[i] += image.lightCDF[i] * imageWeight;
				saturationCDF[i] += image.saturationCDF[i] * imageWeight;
			}
		}
		
		// Compute image light & saturation LUT (Look Up Table)
		for (KeyPointList image : images) {
			for (int i = 0; i < image.lightCDF.length; i++) {
				double cur = image.lightCDF[i];
				int j = lightCDF.length;
				do {
					j--;
				} while ((j > 0) && (cur < lightCDF[j]));
				image.lightCDF[i] = (double) j / (image.lightCDF.length - 1);

				cur = image.saturationCDF[i];
				j = saturationCDF.length;
				do {
					j--;
				} while ((j > 0) && (cur < saturationCDF[j]));
				image.saturationCDF[i] = (double) j / (image.saturationCDF.length - 1);
			}
		}
	}
*/	
	private static final AtomicInteger panoCounter = new AtomicInteger(0);
/*	
	private static class CalcArea extends AbstractConvexHullArea {
		KeyPointPairList pairList;
		int curPoint;
		boolean calcSourceArea;
		
		public CalcArea(KeyPointPairList pairList) {
			this.pairList = pairList;
		}
		
		public void resetPointIterator() {
			curPoint = -1;
		}

		public boolean nextPoint() {
			while (true) {
				curPoint++;
				if (curPoint >= pairList.items.size())
					return false;
				KeyPointPair pair = pairList.items.get(curPoint); 
				if (!pair.panoBad)
					return true;
			}
		}

		public double getX() {
			KeyPointPair pair = pairList.items.get(curPoint); 
			return calcSourceArea ? pair.sourceSP.getDoubleX() : pair.targetSP.getDoubleX();
		}

		public double getY() {
			KeyPointPair pair = pairList.items.get(curPoint); 
			return calcSourceArea ? pair.sourceSP.getDoubleY() : pair.targetSP.getDoubleY();
		}
	}
*/	
	
	public Void call() throws Exception {
		// Print info for all panoramas found
		System.out.println("Panoramas found: " + panos.size());
		images = new ArrayList<KeyPointList>();
		for (int panoIndex = 0; panoIndex < panos.size(); panoIndex++) {
			ArrayList<KeyPointPairList> pano = panos.get(panoIndex);
			CalculatePanoramaParams.buildImagesList(pano, images);
			System.out.println("Panorama " + (panoIndex + 1) + " contains " + images.size() + " images:");
			for (KeyPointList image : images) {
				System.out.println(image.imageFileStamp.getFile().getName());
			}
		}

		for (ArrayList<KeyPointPairList> pano : panos) {
			int panoId = panoCounter.incrementAndGet();
			String outputFile = outputDir + "/pano" + panoId;
			SwtUtil.activeWaitDialogSetStatus("Generating pano " + panoId + "/" + panos.size(), 0);
			Marker.mark("Generate panorama " + panoId);
			images.clear();
			pairLists = pano;
			CalculatePanoramaParams.buildImagesList(pairLists, images);
			calcExtents();
//			calcHistograms();
			
			System.out.println("Output image size in pixels: " + outputImageSizeX + "\t" + outputImageSizeY);

			for (KeyPointList image : images) {
				System.out.println(image.imageFileStamp.getFile().getName() 
						+ "\tminX=" + (int)(image.min.x)
						+ "\tminY=" + (int)(image.min.y)
						+ "\tmaxX=" + (int)(image.max.x)
						+ "\tmaxY=" + (int)(image.max.y)
						);
			}
/*
			for (KeyPointPairList pairList : pano) {
				int helmBadPanoGood = 0;
				int helmBadPanoBad = 0;
				int helmGoodPanoGood= 0;
				int helmGoodPanoBad = 0;
				double maxHelmertDiscrepancy = Double.MIN_VALUE;

				KeyPoint tmpKP = new KeyPoint(pairList.target, 0, 0);
				KeyPointHelmertTransformer tr = new KeyPointHelmertTransformer();
				tr.setParams(pairList.scale, pairList.angle, pairList.translateX, pairList.translateY);
				
				for (KeyPointPair pair : pairList.items) {
					if (pair.panoBad) {
						if (pair.validatePairBad) {
							helmBadPanoBad++;
						} else {
							helmGoodPanoBad++;
						}
					} else {
						tr.transform(pair.sourceSP, tmpKP);
						double d = Math.hypot(tmpKP.getDoubleX() - pair.targetSP.getDoubleX(), tmpKP.getDoubleY() - pair.targetSP.getDoubleY());
						if (maxHelmertDiscrepancy < d) 
							maxHelmertDiscrepancy = d;
						if (pair.validatePairBad) {
							helmBadPanoGood++;
						} else {
							helmGoodPanoGood++;
						}
					}
				}

				CalcArea calcArea = new CalcArea(pairList);
				calcArea.calcSourceArea = true;
				double sourceConvexHullArea = Math.abs(calcArea.getConvexHullArea());
				double sourceImageArea = pairList.source.imageSizeX * pairList.source.imageSizeY;
				double sourceRatio = sourceConvexHullArea / sourceImageArea;
				
				calcArea.calcSourceArea = false;
				double targetConvexHullArea = Math.abs(calcArea.getConvexHullArea());
				double targetImageArea = pairList.target.imageSizeX * pairList.target.imageSizeY;
				double targetRatio = targetConvexHullArea / targetImageArea;
				
				System.out.println(
						pairList.source.imageFileStamp.getFile().getName() + "\t" +
						pairList.target.imageFileStamp.getFile().getName() + "\t" +
						MathUtil.d4(sourceConvexHullArea) + "\t" +
						MathUtil.d4(sourceImageArea) + "\t" +
						MathUtil.d4(sourceRatio) + "\t|" +
						MathUtil.d4(targetConvexHullArea) + "\t" +
						MathUtil.d4(targetImageArea) + "\t" +
						MathUtil.d4(targetRatio) + "\t|" +
						"pairs=" + pairList.items.size() + "\t" +
						"maxHelmertDiscrepancy=" + MathUtil.d2(maxHelmertDiscrepancy) + "\t" +
						"helmBadPanoGood =" + helmBadPanoGood + "\t" + 
						"helmBadPanoBad  =" + helmBadPanoBad + "\t" + 
						"helmGoodPanoGood=" + helmGoodPanoGood + "\t" + 
						"helmGoodPanoBad =" + helmGoodPanoBad 
				);
			}
 */
			outImageColor = new SafeImage(outputImageSizeX, outputImageSizeY);
			outImageColor2 = new SafeImage(outputImageSizeX, outputImageSizeY);
			outImageMask = new SafeImage(outputImageSizeX, outputImageSizeY);
			imageData = new HashMap<KeyPointList, SafeImage>();
			TaskSetExecutor taskSet = new TaskSetExecutor(exec);
			for (int index = 0; index < images.size(); index++) {
				final KeyPointList image = images.get(index);
				taskSet.add(new Callable<Void>() {
					public Void call() throws Exception {
						SafeImage im = new SafeImage(new FileInputStream(image.imageFileStamp.getFile()));
						im.populateStatistics();
						synchronized (imageData) {
							imageData.put(image, im);
						}
						return null;
					}
				});
			}
			taskSet.addFinished();
			taskSet.get();
			
			Runtime runtime = Runtime.getRuntime();
			int numberOfProcessors = runtime.availableProcessors();
			
			taskSet = new TaskSetExecutor(exec);
			rowsProcessed = new AtomicInteger(0);
			for (int i = 0; i < numberOfProcessors; i++) {
				if (useImageMaxWeight) {
//					taskSet.add(new MaxWeightRender(this));
					taskSet.add(new WeightLightRender(this));
				} else {
					taskSet.add(new WeightMergeRender(this));
				}
			}
			taskSet.addFinished();
			taskSet.get();
			
			pinPointPairs(outImageMask);
			labelImageNames(outImageMask);
			outImageColor.save(outputFile + " color.png");
			outImageColor2.save(outputFile + " color2.png");
			outImageMask.save(outputFile + " mask.png");
			
			Marker.release();
		}
		return null;
	}
}
