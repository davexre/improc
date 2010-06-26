package com.slavi.improc.myadjust;

import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.image.DWindowedImageUtils;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.SafeImage;
import com.slavi.math.MathUtil;
import com.slavi.util.ColorConversion;
import com.slavi.util.Marker;
import com.slavi.util.concurrent.TaskSetExecutor;
import com.slavi.util.ui.SwtUtil;

public class GeneratePanoramas implements Callable<Void> {

	ExecutorService exec;
	PanoTransformer panoTransformer;
	ArrayList<ArrayList<KeyPointPairList>> panos;
	
	ArrayList<KeyPointList> images;
	ArrayList<KeyPointPairList> pairLists;

	///////
	
	Map<KeyPointList, SafeImage> imageData = new HashMap<KeyPointList, SafeImage>();
	SafeImage outImageColor;
	SafeImage outImageMask;
	Point2D.Double minAngle = new Point2D.Double();
	Point2D.Double sizeAngle = new Point2D.Double();

	final String outputDir;
	final boolean pinPoints;
	final boolean useColorMasks;
	final boolean useImageMaxWeight;
	int outputImageSizeX = 2000;
	int outputImageSizeY;
	AtomicInteger rowsProcessed;

	public GeneratePanoramas(ExecutorService exec,
			PanoTransformer panoTransformer,
			ArrayList<ArrayList<KeyPointPairList>> panos,
			String outputDir,
			boolean pinPoints,
			boolean useColorMasks,
			boolean useImageMaxWeight) {
		this.exec = exec;
		this.panoTransformer = panoTransformer;
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

	void transformWorldToCamera(double x, double y, KeyPointList image, double dest[]) {
		x = sizeAngle.x * (x / outputImageSizeX) + minAngle.x;
		y = sizeAngle.y * (y / outputImageSizeY) + minAngle.y;
		panoTransformer.transformBackward(x, y, image, dest);
	}
	
	void transformCameraToWorld(double x, double y, KeyPointList image, double dest[]) {
		panoTransformer.transformForeward(x, y, image, dest);
		dest[0] = outputImageSizeX * ((dest[0] - minAngle.x) / sizeAngle.x);
		dest[1] = outputImageSizeY * ((dest[1] - minAngle.y) / sizeAngle.y);
	}

	void pinPoint(int imgX, int imgY, int color, KeyPointList image, SafeImage si, double dest[]) throws InterruptedException {
		if (Thread.currentThread().isInterrupted())
			throw new InterruptedException();
		panoTransformer.transformForeward(imgX, imgY, image, dest);
		// min, max are in radians x (-pi..pi] y [0..pi]
		minMax(dest[0], dest[1], image.min, image.max);
		double x = MathUtil.fixAngle2PI(dest[0]) * si.sizeX / MathUtil.C2PI;
		double y = dest[1] * si.sizeY / Math.PI;
		si.setRGB(outputImageSizeX - 1 - (int) x, (int) y, color);
	}
	
	void calcExtents(String outputfile) throws InterruptedException, IOException {
		int step = 20;
		int bmSizeX = 3600;
		int bmSizeY = 1800;
		double dest[] = new double[3];
		SafeImage si = new SafeImage(bmSizeX, bmSizeY);
		
		for (KeyPointList image : images) {
			image.min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			image.max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
			int color = si.getNextColor();
			for (int i = 0; i < image.imageSizeX; i+=step) {
				for (int j = 0; j < image.imageSizeY; j+=step) {
					pinPoint(i, j, color, image, si, dest);
				}
				pinPoint(i, image.imageSizeY - 1, color, image, si, dest);
			}
			for (int j = 0; j < image.imageSizeY; j+=step) {
				pinPoint(image.imageSizeX - 1, j, color, image, si, dest);
			}
			pinPoint(image.imageSizeX - 1, image.imageSizeY - 1, color, image, si, dest);
		}

		si.save(outputfile);
		
		minAngle.x = Double.POSITIVE_INFINITY;
		minAngle.y = Double.POSITIVE_INFINITY;
		sizeAngle.x = Double.NEGATIVE_INFINITY;
		sizeAngle.y = Double.NEGATIVE_INFINITY;
		for (KeyPointList image : images) {
			minMax(image.min.x, image.min.y, minAngle, sizeAngle);
			minMax(image.max.x, image.max.y, minAngle, sizeAngle);
		}
		sizeAngle.x -= minAngle.x;
		sizeAngle.y -= minAngle.y;
		
		outputImageSizeY = (int)(outputImageSizeX * (sizeAngle.y / sizeAngle.x));
		
		for (KeyPointList image : images) {
			System.out.println(image.imageFileStamp.getFile().getName() 
				+ "\tminX=" + MathUtil.rad2degStr(image.min.x)
				+ "\tminY=" + MathUtil.rad2degStr(image.min.y)
				+ "\tmaxX=" + MathUtil.rad2degStr(image.max.x)
				+ "\tmaxY=" + MathUtil.rad2degStr(image.max.y)
				);
			
			image.min.x = outputImageSizeX * ((image.min.x - minAngle.x) / sizeAngle.x);
			image.min.y = outputImageSizeY * ((image.min.y - minAngle.y) / sizeAngle.y);
			image.max.x = outputImageSizeX * ((image.max.x - minAngle.x) / sizeAngle.x);
			image.max.y = outputImageSizeY * ((image.max.y - minAngle.y) / sizeAngle.y);
		}
	}
	
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
			int x = (int) (outputImageSizeX * ((xd - minAngle.x) / sizeAngle.x));
			for (int j = outputImageSizeY - 1; j >= 0; j--) {
				img.setRGB(outputImageSizeX - 1 - x, j, colorX);
			}
		}
		// draw parallels
		int y = (int) (outputImageSizeY * ((-minAngle.y) / sizeAngle.y));
		for (int i = img.sizeX - 1; i >= 0; i--) {
			img.setRGB(outputImageSizeX - 1 - i, y, 0x00ff00);
		}
	}
	
	static final int masks[][] = { // and mask, or mask 
		{ 0xff0000, 0x000000 },
		{ 0x00ff00, 0x000000 },
		{ 0x0000ff, 0x000000 },
		
		{ 0xff00ff, 0x000000 },
		{ 0xffff00, 0x000000 },
		{ 0x00ffff, 0x000000 },
		
		{ 0xff00ff, 0x007f00 },
		{ 0xffff00, 0x007f00 },
		{ 0x00ffff, 0x7f0000 },

		{ 0xff0000, 0x007f00 },
		{ 0x00ff00, 0x00007f },
		{ 0x0000ff, 0x7f0000 },

		{ 0xff0000, 0x00007f },
		{ 0x00ff00, 0x7f0000 },
		{ 0x0000ff, 0x007f00 },
		
		}; 
	private class ParallelRender implements Callable<Void> {

		int startRow;
		int endRow;
		
		public ParallelRender(int startRow, int endRow) {
			this.startRow = startRow;
			this.endRow = endRow;
		}
		
		public Void call() throws Exception {
			double d[] = new double[3];
			double DRGB[] = new double[3];
			for (int oimgY = startRow; oimgY <= endRow; oimgY++) {
				for (int oimgX = 0; oimgX < outImageColor.sizeX; oimgX++) {
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

					int curMaxWeight = 0;
					int curMaxColor = 0;

					int mcurMaxWeight = 0;
					int mcurMaxColor = 0;
					KeyPointList mcurMaxImage = null;

					for (int index = 0; index < images.size(); index++) {
						if (Thread.currentThread().isInterrupted())
							throw new InterruptedException();
						KeyPointList image = images.get(index);
						
						if (
							(image.min.x > oimgX) || 
							(image.min.y > oimgY) || 
							(image.max.x < oimgX) || 
							(image.max.y < oimgY))
							continue;
						
						SafeImage im = imageData.get(image);
						transformWorldToCamera(oimgX, oimgY, image, d);
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
						if (useImageMaxWeight) {
							if (mcurMaxWeight < weight) {
								mcurMaxWeight = weight;
								mcurMaxImage = image;
								int m[] = masks[index % masks.length];
//								grayColor = grayColor | (grayColor << 8) | (grayColor << 16);
//								mcurMaxColor = (grayColor & m[0]) | m[1] ;
								
								double dgc = grayColor / 255.0;
								mcurMaxColor = 
									((int)(((m[0] >> 16) & (0xff)) * dgc) << 16) |
									((int)(((m[0] >> 8) & (0xff)) * dgc) << 8) |
									((int)(((m[0] >> 0) & (0xff)) * dgc) << 0);								
							}
						} else {
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
						}
						
						// Calculate the color image
						if (useImageMaxWeight) {
							if (curMaxWeight < weight) {
								curMaxWeight = weight;
								curMaxColor = color;
							}
						} else {
							countR += weight;
							countG += weight;
							countB += weight;
							colorR += weight * ((color >> 16) & 0xff);
							colorG += weight * ((color >> 8) & 0xff);
							colorB += weight * (color & 0xff);
						}
					}

					int color = 0;
					if (useImageMaxWeight) {
						outImageColor.setRGB(outputImageSizeX - 1 - oimgX, oimgY, curMaxColor);
						if (mcurMaxImage == null) {
							mcurMaxColor = 0;
						} else {
							double h = mcurMaxImage.imageId * MathUtil.C2PI * 2.0 / 13.0;
							double v = (DWindowedImageUtils.getGrayColor(curMaxColor) & 0xff) / 255.0;
							double s = 1.0;
							ColorConversion.HSV.toDRGB(h, s, v, DRGB);
							mcurMaxColor = ColorConversion.RGB.toRGB(DRGB);
						}
						outImageMask.setRGB(outputImageSizeX - 1 - oimgX, oimgY, mcurMaxColor);
					} else {
						color = 
							(fixColorValue(colorR, countR) << 16) |
							(fixColorValue(colorG, countG) << 8) |
							fixColorValue(colorB, countB);
						outImageColor.setRGB(outputImageSizeX - 1 - oimgX, oimgY, color);
						color = 
							(fixColorValue(mcolorR, mcountR) << 16) |
							(fixColorValue(mcolorG, mcountG) << 8) |
							fixColorValue(mcolorB, mcountB);
						outImageMask.setRGB(outputImageSizeX - 1 - oimgX, oimgY, curMaxColor);
					}
				}
				int processed = rowsProcessed.getAndIncrement();
				if (processed % 10 == 0) {
					SwtUtil.activeWaitDialogSetStatus(null, (100 * processed) / outputImageSizeY);
				}				
			}
			return null;
		}
	}

	private void pinPoints(SafeImage oi) throws InterruptedException {
		double d[] = new double[3];
		for (KeyPointList image : images) {
			int color = oi.getNextColor();
			for (int i = 0; i < image.imageSizeX; i++) {
				transformCameraToWorld(i, 0, image, d);
				oi.setRGB(outputImageSizeX - 1 - (int) d[0], (int) d[1], color);
				transformCameraToWorld(i, image.imageSizeY - 1, image, d);
				oi.setRGB(outputImageSizeX - 1 - (int) d[0], (int) d[1], color);
			}
			
			for (int j = 0; j < image.imageSizeY; j++) {
				transformCameraToWorld(0, j, image, d);
				oi.setRGB(outputImageSizeX - 1 - (int) d[0], (int) d[1], color);
				transformCameraToWorld(image.imageSizeX - 1, j, image, d);
				oi.setRGB(outputImageSizeX - 1 - (int) d[0], (int) d[1], color);
			}
		}
		
		for (KeyPointPairList pairList : pairLists) {
			int colorCross = images.indexOf(pairList.source);
			int colorX = images.indexOf(pairList.target);
			
			if (useImageMaxWeight) {
				int m[] = masks[colorCross % masks.length];
				colorCross = (0xffffff & m[0]) | m[1] ;
				
				m = masks[colorX % masks.length];
				colorX = (0xffffff & m[0]) | m[1] ;
			} else {
				colorCross = colorCross % 3;
				colorCross = colorCross < 0 ? -1 : 255 << (8 * colorCross);
				
				colorX = colorX % 3;
				colorX = colorX < 0 ? -1 : 255 << (8 * colorX);			
			}
			
			
			for (KeyPointPair pair : pairList.items) {
				if (!pair.bad) {
					transformCameraToWorld(pair.sourceSP.doubleX, pair.sourceSP.doubleY, pairList.source, d);
					int x1 = (int)d[0];
					int y1 = (int)d[1];
					transformCameraToWorld(pair.targetSP.doubleX, pair.targetSP.doubleY, pairList.target, d);
					int x2 = (int)d[0];
					int y2 = (int)d[1];
					oi.pinPair(x1, y1, x2, y2, colorCross, colorX);
				}
			}
		}
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

		out.println("minAngle.x=" + MathUtil.rad2degStr(minAngle.x));
		out.println("minAngle.y=" + MathUtil.rad2degStr(minAngle.y));
		out.println("sizeAngle.x=" + MathUtil.rad2degStr(sizeAngle.x));
		out.println("sizeAngle.y=" + MathUtil.rad2degStr(sizeAngle.y));
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
	
	private static final AtomicInteger panoCounter = new AtomicInteger(0);
	
	public Void call() throws Exception {
		System.out.println("Panoramas found: " + panos.size());
		images = new ArrayList<KeyPointList>();
		for (int panoIndex = 0; panoIndex < panos.size(); panoIndex++) {
			ArrayList<KeyPointPairList> pano = panos.get(panoIndex);
			CalculatePanoramaParams.buildImagesList(pano, images);
			System.out.println("Panorama " + panoIndex + " contains " + images.size() + " images:");
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
			calcExtents(outputFile + " mask extent.png");

			System.out.println("MIN Angle X,Y:  " + MathUtil.rad2degStr(minAngle.x) + "\t" + MathUtil.rad2degStr(minAngle.y));
			System.out.println("SIZE angle X,Y: " + MathUtil.rad2degStr(sizeAngle.x) + "\t" + MathUtil.rad2degStr(sizeAngle.y));
			System.out.println("Size in pixels: " + outputImageSizeX + "\t" + outputImageSizeY);

			for (KeyPointList image : images) {
//				System.out.println(image.imageFileStamp.getFile().getName() 
//						+ "\tminX=" + MathUtil.rad2degStr(image.min.x)
//						+ "\tminY=" + MathUtil.rad2degStr(image.min.y)
//						+ "\tmaxX=" + MathUtil.rad2degStr(image.max.x)
//						+ "\tmaxY=" + MathUtil.rad2degStr(image.max.y)
//						);
				System.out.println(image.imageFileStamp.getFile().getName() 
						+ "\tminX=" + (int)(image.min.x)
						+ "\tminY=" + (int)(image.min.y)
						+ "\tmaxX=" + (int)(image.max.x)
						+ "\tmaxY=" + (int)(image.max.y)
						);
			}
			
			outImageColor = new SafeImage(outputImageSizeX, outputImageSizeY);
			outImageMask = new SafeImage(outputImageSizeX, outputImageSizeY);
			imageData = new HashMap<KeyPointList, SafeImage>();
			for (int index = 0; index < images.size(); index++) {
				KeyPointList image = images.get(index);
				SafeImage im = new SafeImage(new FileInputStream(image.imageFileStamp.getFile()));
				imageData.put(image, im);
			}
			
			Runtime runtime = Runtime.getRuntime();
			int numberOfProcessors = runtime.availableProcessors();
			
			int dY = Math.max(outputImageSizeY / numberOfProcessors, 10);
			if (outputImageSizeY % numberOfProcessors != 0) {
				dY++;
			}
			
			int startRow = 0;
			TaskSetExecutor taskSet = new TaskSetExecutor(exec);
			rowsProcessed = new AtomicInteger(0);
			while (startRow < outputImageSizeY) {
				int endRow = Math.min(startRow + dY - 1, outputImageSizeY - 1);
				ParallelRender task = new ParallelRender(startRow, endRow);
				taskSet.add(task);
				startRow = endRow + 1;
			}
			taskSet.addFinished();
			taskSet.get();
			
//			drawWorldMesh(outImageMask);
			pinPoints(outImageMask);
			
			outImageColor.save(outputFile + " color.png");
			outImageMask.save(outputFile + " mask.png");
			
			Marker.release();
			dumpPointData(outputFile + ".txt");
		}
		return null;
	}
}
