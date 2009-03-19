package com.slavi.improc.old;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.slavi.image.BufferedBMPImage;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.AffineTransformer;

public class AdjAffine {
	
	public static class AdjAffineTransformer extends AffineTransformer<Point2D.Double, Point2D.Double> {
		public int getInputSize() {
			return 2;
		}

		public int getOutputSize() {
			return 2;
		}

		private double getCoord(Point2D.Double item, int coordIndex) {
			switch (coordIndex) {
				case 0: return item.x;
				case 1: return item.y;
				default: throw new RuntimeException("Invalid coordinate");
			}
		}

		private void setCoord(Point2D.Double item, int coordIndex, double value) {
			switch (coordIndex) {
				case 0: 
					item.x = value;
					break;				
				case 1: 
					item.y = value;
					break;
				default: throw new RuntimeException("Invalid coordinate");
			}
		}
		
		public double getSourceCoord(Point2D.Double item, int coordIndex) {
			return getCoord(item, coordIndex);
		}

		public double getTargetCoord(Point2D.Double item, int coordIndex) {
			return getCoord(item, coordIndex);
		}

		public void setSourceCoord(Point2D.Double item, int coordIndex, double value) {
			setCoord(item, coordIndex, value);
		}

		public void setTargetCoord(Point2D.Double item, int coordIndex, double value) {
			setCoord(item, coordIndex, value);
		}
	}
	
	/**
	 * Middle point of the {@link #worldExtent}.
	 * This value is computed by {@link #calcImageExtents()}
	 */
	double midX, midY;
	
	/**
	 * The union of the extents of the images in World coordinate system.
	 * This value is computed by {@link #calcImageExtents()}
	 */
	Rectangle2D.Double worldExtent;

	public static class AdjImage {
		public String imageFile;
		
		public Matrix toWorld = null;
		
		public Matrix fromWorld = null;
		
		/**
		 * The squared value of fromWorld 
		 */
		public Matrix fromWorld2 = null;
		
		/**
		 * Extent of the image in image coordinate system. The origin of the extent is always the 0,0 point
		 */
		public Rectangle2D.Double extent;
		
		/**
		 * The extent of the image in World coordinate system. 
		 * This value is computed by {@link #AdjAffine.calcImageExtents()}
		 */
		public Rectangle2D.Double worldExtent;
		
		/**
		 * Middle point of the extent {@link #worldExtent}
		 * This value is computed by {@link #AdjAffine.calcImageExtents()}
		 */
		public double midWX, midWY;		
		
		public String toString() {
			return "IMG=" + imageFile + "\tW=" + extent.width + "\tH=" + extent.height + "\t" + 
				"\tExtW=" + worldExtent.width + "\tExtH=" + worldExtent.height + "\ttoWorld=" + toWorld.toMatlabString("toWorld");
		}
	}
	
	public static class AdjImagePair {
		public AdjImage source = null;
		 
		public AdjImage target = null;
		 
		public Matrix toTarget = null;
		 
		public Matrix fromTarget = null;
		 
		public AdjImagePair(AdjImage source, AdjImage target, Matrix toTarget) {
			this.source = source;
			this.target = target;
			this.toTarget = toTarget;
			this.fromTarget = toTarget.makeCopy();
			if (!this.fromTarget.inverse())
				throw new IllegalArgumentException("Invalid transformation matrix specified"); 
		}
		
		public String toString() {
			return 
				"SRC=" + source.imageFile +
				"\tTRG=" + target.imageFile +
				"\t" + toTarget.toMatlabString("M");
		}
	}
	
	private ArrayList<AdjImagePair> ipl = new ArrayList<AdjImagePair>();
	
	private ArrayList<AdjImage> il = new ArrayList<AdjImage>();
	
	private AdjImage addImage(String imageFile, int width, int height) {
		for (AdjImage i : il)
			if (i.imageFile.equalsIgnoreCase(imageFile))
				return i;
		AdjImage r = new AdjImage();
		r.imageFile = imageFile;
		r.extent = new Rectangle2D.Double(0, 0, width, height);
		r.worldExtent = new Rectangle2D.Double(0, 0, width, height);
		r.midWX = width / 2;
		r.midWY = height / 2;
		il.add(r);
		return r;
	}
	
	/**
	 * Set ppl to the value returned by {@link PanoList#getImageChain()}
	 */
	public void initWithPanoList(ArrayList<PanoPairList> ppl) {
		// Build the image list.
		ipl.clear();
		il.clear();
		for (PanoPairList p : ppl) {
			AdjImagePair ip = new AdjImagePair(
				addImage(p.sourceImage, p.sourceImageSizeX, p.sourceImageSizeY),
				addImage(p.targetImage, p.targetImageSizeX, p.targetImageSizeY),
				p.transform.getMatrix());
			ipl.add(ip);
		}
	}
	
	private void printImageNames() {
		System.out.println("*** Images");
		for (int i = 0; i < il.size(); i++) {
			AdjImage ii = il.get(i);
			System.out.println("a" + i + "\t" + ii.imageFile);
		}
	}
	
	private void printImageToWorld() {
		System.out.println("*** Image toWorld");
		for (int i = 0; i < il.size(); i++) {
			AdjImage ii = il.get(i);
			System.out.println(ii.toWorld.toMatlabString("a" + i));
		}
	}
	
	private void printSourceToTarget() {
		System.out.println("*** source to target");
		for (AdjImagePair p : ipl) {
			System.out.println(p.toTarget.toMatlabString(
					"a" + il.indexOf(p.source) + 
					"a" + il.indexOf(p.target)));
		}
	}
	
	public void dump4matlab() {
		printImageNames();
		printImageToWorld();
		printSourceToTarget();
	}
	
	private void calcToWorldUsingOriginImage(AdjImage image) {
		// Clear previous calculations
		for (AdjImage i : il)
			i.toWorld = null;
		image.toWorld = new Matrix(3, 3);
		image.toWorld.makeE();
		
		// Calculate the approximate toWorld matrices
		boolean loopMore = true;
		do {
			loopMore = false;
			for (AdjImagePair p : ipl) {
				if ((p.source.toWorld != null) && (p.target.toWorld == null)) {
					p.target.toWorld = new Matrix(3, 3);
					p.fromTarget.mMul(p.source.toWorld, p.target.toWorld);
					p.target.toWorld.setItem(0, 2, 0.0);
					p.target.toWorld.setItem(1, 2, 0.0);
					p.target.toWorld.setItem(2, 2, 1.0);
					loopMore = true;
				} else if ((p.source.toWorld == null) && (p.target.toWorld != null)) {
					p.source.toWorld = new Matrix(3, 3);
					p.toTarget.mMul(p.target.toWorld, p.source.toWorld);
					p.source.toWorld.setItem(0, 2, 0.0);
					p.source.toWorld.setItem(1, 2, 0.0);
					p.source.toWorld.setItem(2, 2, 1.0);
					loopMore = true;
				} 
			}
		} while(loopMore);
	}

	private void calcFromWorld() {
		// Check if there are any non-computed images
		// Calculate all the "fromWorld" matrices
		for (AdjImage i : il) { 
			if (i.toWorld == null)
				throw new IllegalArgumentException("There are not connected images");
			i.fromWorld = i.toWorld.makeCopy();
			if (!i.fromWorld.inverse())
				throw new IllegalArgumentException("Could not compute the fromWorld matrix");
			i.fromWorld.setItem(0, 2, 0.0);
			i.fromWorld.setItem(1, 2, 0.0);
			i.fromWorld.setItem(2, 2, 1.0);
			i.fromWorld2 = new Matrix(3, 3);
			i.fromWorld.mMul(i.fromWorld, i.fromWorld2);
		}
	}
	
	private Matrix getRotationMatrix(Matrix m) {
		Matrix r = m.makeCopy();
		r.setItem(2, 0, 0.0);
		r.setItem(2, 1, 0.0);
		r.setItem(2, 2, 1.0);
		r.setItem(0, 2, 0.0);
		r.setItem(1, 2, 0.0);
		return r;
	}
	
	private void calcUsingOriginPoint(AdjImage image) {
		printImageNames();
		printSourceToTarget();

		System.out.println("*** Calc using origin image " + il.indexOf(image));
		calcToWorldUsingOriginImage(image);
		calcFromWorld();

		printImageToWorld();
		
		// Least square adjust the rotation of the images 
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(il.size() * 4);
		Matrix coefs = new Matrix(il.size() * 4, 1);
		Matrix L = new Matrix(3, 3);
		Matrix Q = new Matrix(3, 3);
		Matrix a = new Matrix(3, 3);
		Matrix b = new Matrix(3, 3);
		for (AdjImagePair p : ipl) {
			System.out.println();
			System.out.println("------ Processing pair " + il.indexOf(p.source) + "-" + il.indexOf(p.target));
			int iA = il.indexOf(p.source) * 4;
			int iB = il.indexOf(p.target) * 4;
			// Discrepancy
			Matrix rotSrcToWorld = getRotationMatrix(p.source.toWorld);
			Matrix rotTargetFromWorld = getRotationMatrix(p.target.fromWorld);
			Matrix rotMul = new Matrix();
			rotSrcToWorld.mMul(rotTargetFromWorld, rotMul);
			rotMul.mSub(getRotationMatrix(p.toTarget), L);
			L.printM("L");

			for (int i = 0; i < 2; i++) { 
				for (int j = 0; j < 2; j++) {
					Q.make0();
					Q.setItem(i, j, 1.0);
		
					rotMul.mMul(Q, a);
					a.mMul(rotTargetFromWorld, b);
					Q.mMul(rotTargetFromWorld, a);
					coefs.make0();
		
					coefs.setItem(iA + 0, 0, a.getItem(0, 0));
					coefs.setItem(iA + 1, 0, a.getItem(1, 0));
					coefs.setItem(iA + 2, 0, a.getItem(0, 1));
					coefs.setItem(iA + 3, 0, a.getItem(1, 1));
		
					coefs.setItem(iB + 0, 0, -b.getItem(0, 0));
					coefs.setItem(iB + 1, 0, -b.getItem(1, 0));
					coefs.setItem(iB + 2, 0, -b.getItem(0, 1));
					coefs.setItem(iB + 3, 0, -b.getItem(1, 1));
					lsa.addMeasurement(coefs, 1.0, L.getItem(i, j), 0);
				}
			}
		}
		lsa.calculate();

		// Recreate the toWorld and fromWorld matrices
		Matrix u = lsa.getUnknown();
		for (int i = il.size() - 1; i >= 0; i--) {
			AdjImage im = il.get(i);
			int iA = i * 4;
			im.toWorld.setItem(0, 0, u.getItem(0, iA + 0));
			im.toWorld.setItem(1, 0, u.getItem(0, iA + 1));
			im.toWorld.setItem(0, 1, u.getItem(0, iA + 2));
			im.toWorld.setItem(1, 1, u.getItem(0, iA + 3));
		}
		printImageToWorld();
		calcFromWorld();
/*		dump4matlab();

		// Least square adjust the translation of the images 
		lsa = new LeastSquaresAdjust(il.size() * 2, 2);
		coefs = new Matrix(il.size() * 2, 1);
		a = new Matrix(1, 3);
		a.setItem(0, 2, 0.0);
		b = new Matrix(1, 3);
		L = new Matrix(1, 3);
		for (AdjImagePair p : ipl) {
			int iA = il.indexOf(p.source);
			int iB = il.indexOf(p.target);
			// Discrepancy
			a.setItem(0, 0, p.toTarget.getItem(2, 0));
			a.setItem(0, 1, p.toTarget.getItem(2, 1));
			p.source.toWorld.mMul(a, b);
			
			coefs.make0();
			coefs.setItem(iA, 0, 1.0);
			coefs.setItem(iB, 0, -1.0);
			lsa.addMeasurement(coefs, 1.0, p.source.toWorld.getItem(2, 0) - p.target.toWorld.getItem(2, 0) + b.getItem(0, 0), 0);
			lsa.addMeasurement(coefs, 1.0, p.source.toWorld.getItem(2, 1) - p.target.toWorld.getItem(2, 1) + b.getItem(0, 1), 1);
		}
		lsa.calculate();
		
		// Recreate the toWorld and fromWorld matrices
		u = lsa.getUnknown();
		for (int iA = il.size() - 1; iA >= 0; iA--) {
			AdjImage im = il.get(iA);
			im.toWorld.setItem(0, 0, u.getItem(0, iA));
			im.toWorld.setItem(1, 0, u.getItem(1, iA));
		}
		calcFromWorld();
*/
	}
	
	/**
	 * Transforms the source rectangle vertices into the destination 
	 * coordinate system and returns the extent of the transformed vertices.  
	 */
	public void transformExtent(AdjAffineTransformer tr, Rectangle2D.Double source, Rectangle2D.Double dest) {
		if ((tr.getInputSize() != 2) || (tr.getOutputSize() != 2))
			throw new UnsupportedOperationException("Tranfsorm extent is 2D only operation");
		
		Point2D.Double s = new Point2D.Double();
		Point2D.Double d = new Point2D.Double();
		double minX, minY, maxX, maxY, t;
		
		s.x = source.x;
		s.y = source.y;
		tr.transform(s, d);
		minX = maxX = d.x;
		minY = maxY = d.y;

		s.x = source.x + source.width;
		s.y = source.y;
		tr.transform(s, d);
		t = d.x;
		if (minX > t) minX = t;
		if (maxX < t) maxX = t;
		t = d.y;
		if (minY > t) minY = t;
		if (maxY < t) maxY = t;
		
		s.x = source.x;
		s.y = source.y + source.height;
		tr.transform(s, d);
		t = d.x;
		if (minX > t) minX = t;
		if (maxX < t) maxX = t;
		t = d.y;
		if (minY > t) minY = t;
		if (maxY < t) maxY = t;

		s.x = source.x + source.width;
		s.y = source.y + source.height;
		tr.transform(s, d);
		t = d.x;
		if (minX > t) minX = t;
		if (maxX < t) maxX = t;
		t = d.y;
		if (minY > t) minY = t;
		if (maxY < t) maxY = t;
		
		dest.x = minX;
		dest.y = minY;
		dest.width = maxX - minX;
		dest.height = maxY - minY;
	}
	
	public void calcImageExtents() {
		AdjAffineTransformer tr = new AdjAffineTransformer();
		worldExtent = null;
		for (AdjImage i : il) {
			tr.setMatrix(i.toWorld);
			transformExtent(tr, i.extent, i.worldExtent);
			// Calculate the mid point of the extent of all "transformed image" extents
			i.midWX = i.worldExtent.getCenterX();
			i.midWY = i.worldExtent.getCenterY();
			// Calculate the worldExtent of all images
			worldExtent = (worldExtent == null ? i.worldExtent : 
				(Rectangle2D.Double) worldExtent.createUnion(i.worldExtent));
		}
		midX = worldExtent.getCenterX();
		midY = worldExtent.getCenterY();
	}
	
	private void dumpImageExtents() {
		System.out.println("*** Image extents");
		for (AdjImage i : il) {
			System.out.println(i);
		}
	}
	
	public void flattenImages1(String outputFileName) throws IOException {
		BufferedBMPImage bo = BufferedBMPImage.create(new File(outputFileName), (int)worldExtent.width, (int)worldExtent.height);
		// Clear the output image
		for (int j = bo.minY(); j <= bo.maxY(); j++)
			for (int i = bo.minX(); i <= bo.maxX(); i++)
				bo.setPixel(i, j, 0);
		
		AdjAffineTransformer transform = new AdjAffineTransformer();
		Point2D.Double srcPoint1 = new Point2D.Double(0.0, 0.0);
		Point2D.Double srcPoint2 = new Point2D.Double(1.0, 1.0);
		Point2D.Double destPoint1 = new Point2D.Double();
		Point2D.Double destPoint2 = new Point2D.Double();
		for (AdjImage adjImage : il) {
			System.out.println("Now processing image " + adjImage.imageFile);
			BufferedImage bi = ImageIO.read(new File(adjImage.imageFile));
			transform.setMatrix(adjImage.fromWorld);
			transform.transform(srcPoint1, destPoint1);
			transform.transform(srcPoint2, destPoint2);
			double radius = 0.5 * Math.sqrt(
					Math.pow(destPoint2.x - destPoint1.x, 2.0) + 
					Math.pow(destPoint2.y - destPoint1.y, 2.0));
			int minX = (int)adjImage.worldExtent.getMinX();
			int maxX = (int)adjImage.worldExtent.getMaxX();
			int minY = (int)adjImage.worldExtent.getMinY();
			int maxY = (int)adjImage.worldExtent.getMaxY();
			for (int j = minY; j <= maxY; j++) {
				for (int i = minX; i <= maxX; i++) {
					srcPoint1.x = i;
					srcPoint1.y = j;
					transform.transform(srcPoint1, destPoint1);
					int imgMinX = (int) (destPoint1.x - radius);
					int imgMaxX = (int) (destPoint1.x + radius);
					int imgMinY = (int) (destPoint1.y - radius);
					int imgMaxY = (int) (destPoint1.y + radius);
					
					int r = 0;
					int g = 0;
					int b = 0;
					int count = 0;
					for (int imgJ = imgMinY; imgJ <= imgMaxY; imgJ++)
						for (int imgI = imgMinX; imgI <= imgMaxX; imgI++) {
							int c = bi.getRGB(imgI, imgJ);
							r += (c & 0xff0000) >> 16;
							g += (c & 0x00ff00) >> 8;
							b += (c & 0x0000ff);
							count++;
						}
					r = (r / count) & 0xff;  
					g = (g / count) & 0xff;  
					b = (b / count) & 0xff;  
					int c = (r << 16) | (g << 8) | (b);
					bo.setPixel(i, j, c);
				}
			}
		}
		bo.close();
	}
	
	public void flattenImages2(String outputFileName) throws IOException {
		int width = (int)worldExtent.width;
		int height = (int)worldExtent.height;

		BufferedBMPImage bo = BufferedBMPImage.create(new File(outputFileName), width, height);
		short buf[][][] = new short[2][width][height];
		
		Matrix src = new Matrix(1, 3);
		Matrix dest = new Matrix(1, 3);
		src.setItem(0, 2, 1.0);
		for (int color = 0; color < 3; color++) {
			// Clear the buffers for the current color space
			for (int i = 0; i < width; i++)
				for (int j = 0; j < height; j++) 
					buf[0][i][j] = buf[1][i][j] = 0;
			
			int shift = color * 8;

			for (AdjImage img : il) {
				BufferedImage bi = ImageIO.read(new File(img.imageFile));
				for (int i = bi.getWidth() - 1; i >= 0; i--) {
					src.setItem(0, 0, i);
					for (int j = bi.getHeight() - 1; j >= 0; j--) {
						src.setItem(0, 1, j);
						img.toWorld.mMul(src, dest);
						int di = (int)(dest.getItem(0, 0));
						int dj = (int)(dest.getItem(0, 1));
						if ( (di < 0) || (dj < 0) || (di >= width) || (dj >= height) )
							continue;
						int c = (bi.getRGB(i, j) >> shift) & 0xff;
						// process the color
						buf[0][di][dj]++;
						buf[1][di][dj] += c;
					}
				}
			}
			
			// Flatten the current color space and copy it to the resulting image
/*			int mask = ~(0xff << shift);
			for (int i = 0; i < width; i++)
				for (int j = 0; j < height; j++) {
					int c = bo.getRGB(i, j);
					int d = buf[0][i][j] == 0 ? 0 : buf[1][i][j] / buf[0][i][j];
					if (d < 0) d = 0;
					if (d > 255) d = 255;
					c = (c & mask) | (d << shift);
					bo.setRGB(i, j, c);
				} */
			int mask = ~(0xff << shift);
			for (int i = 0; i < width; i++)
				for (int j = 0; j < height; j++) {
					int c = bo.getPixel(i, j);
					int d = buf[0][i][j] == 0 ? 0 : buf[1][i][j] / buf[0][i][j];
					if (d < 0) d = 0;
					if (d > 255) d = 255;
					c = (c & mask) | (d << shift);
					bo.setPixel(i, j, c);
				}			
			
		}
		bo.close();
	}
	
	public void doTheJob() {
		if ( (il.size() < 2) || (il.size() > ipl.size()))
			return;		
		
		// Assume the "first" image to be the "correct" one and orient 
		// all the others to this one
		calcUsingOriginPoint(il.get(0));
		
		// Calculate image extents in the world coordinate system
		calcImageExtents();
		dumpImageExtents();

		// Find the closest image to the mid point 
		double midDist = Double.MAX_VALUE;
		AdjImage midImage = null;
		for (AdjImage i : il) {
			double dx = i.midWX - midX;
			double dy = i.midWY - midY;
			double d = dx * dx + dy * dy;
			if (midDist > d) {
				midDist = d;
				midImage = i;
			}
		}

		// Recalculate using the new origin point
		calcUsingOriginPoint(midImage);
		// Calculate image rectangles in the world coordinate system
		calcImageExtents();
		dumpImageExtents();
		
		// Determine the extent of all transformed image extents
		System.out.println(worldExtent);
		int width = (int)(worldExtent.width);
		int height = (int)(worldExtent.height);
		System.out.println("WIDTH = " + width);
		System.out.println("HEIGHT= " + height);

//		flattenImages1(Const.outputImage);

		// Dump results
		for (int i = 0; i < il.size(); i++) {
		    AdjImage img = il.get(i);
			System.out.println(img.toWorld.toMatlabString("A" + i));
			System.out.println(img.fromWorld.toMatlabString("R" + i));
		}
	}
}
