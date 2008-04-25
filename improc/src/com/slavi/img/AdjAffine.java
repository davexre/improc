package com.slavi.img;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.slavi.matrix.Matrix;
import com.slavi.statistics.LeastSquaresAdjust;

public class AdjAffine {
	public static class AdjImage {
		public String imageFile;
		
		public Matrix toWorld = null;
		
		public Matrix fromWorld = null;
		
		public Matrix fromWorld2 = null;
		
		public double width, height;
		
		public double x1 = 0.0, y1 = 0.0;	// The point (0, 0) in image space
		
		public double x2 = 0.0, y2 = 0.0;	// The point (width, height) in image space
		
		public double midWX, midWY;		// The middle point of the immage in world coordinates
		
		public String toString() {
			return "IMG=" + imageFile + "\tW=" + width + "\tH=" + height + "\t" + toWorld.toMatlabString("toWorld");
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
		r.width = width;
		r.height = height;
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
			//System.out.println(ip);
			
		}
	}
	
	private void dump4matlab() {
		System.out.println("*** Images");
		for (int i = 0; i < il.size(); i++) {
			AdjImage ii = il.get(i);
			System.out.println("a" + i + "\t" + ii.imageFile);
		}
		System.out.println("*** Image toWorld");
		for (int i = 0; i < il.size(); i++) {
			AdjImage ii = il.get(i);
			System.out.println(ii.toWorld.toMatlabString("a" + i));
		}
		System.out.println("*** source to target");
		for (AdjImagePair p : ipl) {
			System.out.println(p.toTarget.toMatlabString(
					"a" + il.indexOf(p.source) + 
					"a" + il.indexOf(p.target)));
		}
	}
	
	private void calcUsingOriginPoint(AdjImage image) {
		// Clear previous calculcations
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
					loopMore = true;
				} else if ((p.source.toWorld == null) && (p.target.toWorld != null)) {
					p.source.toWorld = new Matrix(3, 3);
					p.toTarget.mMul(p.target.toWorld, p.source.toWorld);
					loopMore = true;
				} 
			}
		} while(loopMore);
		
		// Check if there are any non-computed images
		// Calculate all the "fromWorld" matrices
		for (AdjImage i : il) { 
			if (i.toWorld == null)
				throw new IllegalArgumentException("There are not connected images");
			i.fromWorld = i.toWorld.makeCopy();
			if (!i.fromWorld.inverse())
				throw new IllegalArgumentException("Could not compute the fromWorld matrix");
			i.fromWorld2 = new Matrix(3, 3);
			i.fromWorld.mMul(i.fromWorld, i.fromWorld2);
		}

		dump4matlab();
		
		// Least square adrjust the images 
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(il.size() * 6);
		Matrix coefs = new Matrix(il.size() * 6, 1);
//		Matrix m = new Matrix(3, 3);
		Matrix L = new Matrix(3, 3);
		Matrix Q = new Matrix(3, 3);
		Matrix a = new Matrix(3, 3);
		Matrix b = new Matrix(3, 3);
		for (AdjImagePair p : ipl) {
			int iA = il.indexOf(p.source) * 6;
			int iB = il.indexOf(p.target) * 6;
			// Discrepancy
			p.source.toWorld.mMul(p.target.fromWorld, L);
			L.mSub(p.toTarget, L);

			for (int i = 0; i < 3; i++) { 
				for (int j = 0; j < 2; j++) {
					Q.make0();
					Q.setItem(i, j, 1.0);
		
					p.source.toWorld.mMul(p.target.fromWorld, b);
					b.mMul(Q, a);
					a.mMul(p.target.fromWorld, b);
					Q.mMul(p.target.fromWorld, a);
					coefs.make0();
		
					coefs.setItem(iA + 0, 0, a.getItem(0, 0));
					coefs.setItem(iA + 1, 0, a.getItem(1, 0));
					coefs.setItem(iA + 2, 0, a.getItem(2, 0));
					coefs.setItem(iA + 3, 0, a.getItem(0, 1));
					coefs.setItem(iA + 4, 0, a.getItem(1, 1));
					coefs.setItem(iA + 5, 0, a.getItem(2, 1));
		
					coefs.setItem(iB + 0, 0, -b.getItem(0, 0));
					coefs.setItem(iB + 1, 0, -b.getItem(1, 0));
					coefs.setItem(iB + 2, 0, -b.getItem(2, 0));
					coefs.setItem(iB + 3, 0, -b.getItem(0, 1));
					coefs.setItem(iB + 4, 0, -b.getItem(1, 1));
					coefs.setItem(iB + 5, 0, -b.getItem(2, 1));
					lsa.addMeasurement(coefs, 1.0, L.getItem(i, j), 0);
				}
			}
			
/*			
			p.source.toWorld.mMul(p.target.fromWorld2, m);

			coefs.make0();
			coefs.setItem(iA + 0, 0, p.target.fromWorld.getItem(0, 0));
			coefs.setItem(iA + 1, 0, p.target.fromWorld.getItem(1, 0));
			coefs.setItem(iA + 2, 0, p.target.fromWorld.getItem(2, 0));
			coefs.setItem(iA + 3, 0, p.target.fromWorld.getItem(0, 1));
			coefs.setItem(iA + 4, 0, p.target.fromWorld.getItem(1, 1));
			coefs.setItem(iA + 5, 0, p.target.fromWorld.getItem(2, 1));
			coefs.setItem(iB + 0, 0, -m.getItem(0, 0));
			coefs.setItem(iB + 1, 0, -m.getItem(1, 0));
			coefs.setItem(iB + 2, 0, -m.getItem(2, 0));
			coefs.setItem(iB + 3, 0, -m.getItem(0, 1));
			coefs.setItem(iB + 4, 0, -m.getItem(1, 1));
			coefs.setItem(iB + 5, 0, -m.getItem(2, 1));
			lsa.addMeasurement(coefs, 1.0, L.getItem(0, 0), 0);
			lsa.addMeasurement(coefs, 1.0, L.getItem(1, 0), 0);
			lsa.addMeasurement(coefs, 1.0, L.getItem(2, 0), 0);
			lsa.addMeasurement(coefs, 1.0, L.getItem(0, 1), 0);
			lsa.addMeasurement(coefs, 1.0, L.getItem(1, 1), 0);
			lsa.addMeasurement(coefs, 1.0, L.getItem(2, 1), 0);
*/			
/*			
			coefs.make0();
			coefs.setItem(iA + 0, 0, p.target.fromWorld.getItem(0, 0));
			coefs.setItem(iA + 1, 0, p.target.fromWorld.getItem(1, 0));
			coefs.setItem(iB + 0, 0, -m.getItem(0, 0));
			coefs.setItem(iB + 3, 0, -m.getItem(0, 1));
			lsa.addMeasurement(coefs, 1.0, L.getItem(0, 0), 0);
			
			coefs.make0();
			coefs.setItem(iA + 0, 0, p.target.fromWorld.getItem(0, 0));
			coefs.setItem(iA + 1, 0, p.target.fromWorld.getItem(1, 0));
			coefs.setItem(iB + 1, 0, -m.getItem(1, 0));
			coefs.setItem(iB + 4, 0, -m.getItem(1, 1));
			lsa.addMeasurement(coefs, 1.0, L.getItem(1, 0), 0);

			coefs.make0();
			coefs.setItem(iA + 0, 0, p.target.fromWorld.getItem(0, 0));
			coefs.setItem(iA + 1, 0, p.target.fromWorld.getItem(1, 0));
			coefs.setItem(iA + 2, 0, p.target.fromWorld.getItem(2, 0));
			coefs.setItem(iB + 2, 0, -m.getItem(2, 0));
			coefs.setItem(iB + 5, 0, -m.getItem(2, 1));
			lsa.addMeasurement(coefs, 1.0, L.getItem(2, 0), 0);
			
			coefs.make0();
			coefs.setItem(iA + 3, 0, p.target.fromWorld.getItem(0, 1));
			coefs.setItem(iA + 4, 0, p.target.fromWorld.getItem(1, 1));
			coefs.setItem(iB + 0, 0, -m.getItem(0, 0));
			coefs.setItem(iB + 3, 0, -m.getItem(0, 1));
			lsa.addMeasurement(coefs, 1.0, L.getItem(0, 1), 0);
			
			coefs.make0();
			coefs.setItem(iA + 3, 0, p.target.fromWorld.getItem(0, 1));
			coefs.setItem(iA + 4, 0, p.target.fromWorld.getItem(1, 1));
			coefs.setItem(iB + 1, 0, -m.getItem(1, 0));
			coefs.setItem(iB + 4, 0, -m.getItem(1, 1));
			lsa.addMeasurement(coefs, 1.0, L.getItem(1, 1), 0);

			coefs.make0();
			coefs.setItem(iA + 3, 0, p.target.fromWorld.getItem(0, 1));
			coefs.setItem(iA + 4, 0, p.target.fromWorld.getItem(1, 1));
			coefs.setItem(iA + 5, 0, p.target.fromWorld.getItem(2, 1));
			coefs.setItem(iB + 2, 0, -m.getItem(2, 0));
			coefs.setItem(iB + 5, 0, -m.getItem(2, 1));
			lsa.addMeasurement(coefs, 1.0, L.getItem(2, 1), 0);
*/
		}
		lsa.calculate();

		// Recreate the toWorld and fromWorld matrices
		Matrix u = lsa.getUnknown();
		for (int i = il.size() - 1; i >= 0; i--) {
			AdjImage im = il.get(i);
			int iA = i * 6;
			im.toWorld.setItem(0, 0, u.getItem(0, iA + 0));
			im.toWorld.setItem(1, 0, u.getItem(0, iA + 1));
			im.toWorld.setItem(2, 0, u.getItem(0, iA + 2));
			im.toWorld.setItem(0, 1, u.getItem(0, iA + 3));
			im.toWorld.setItem(1, 1, u.getItem(0, iA + 4));
			im.toWorld.setItem(2, 1, u.getItem(0, iA + 5));
			im.toWorld.setItem(0, 2, 0.0);
			im.toWorld.setItem(1, 2, 0.0);
			im.toWorld.setItem(2, 2, 1.0);
			
			im.toWorld.copyTo(im.fromWorld);
			if (!im.fromWorld.inverse())
				throw new Error("After adjustment can not calculate the reverse affine transofrmation");
		}
		dump4matlab();		
	}
	
	public void calcImageExtents() {
		Matrix src = new Matrix(1, 3);
		Matrix dest = new Matrix(1, 3);
		src.setItem(0, 2, 1.0);
		for (AdjImage i : il) {
			// calc the origin (0, 0) point
			i.x1 = i.toWorld.getItem(2, 0);
			i.y1 = i.toWorld.getItem(2, 1);
			// calc the (width, height) point
			src.setItem(0, 0, i.width);
			src.setItem(0, 1, i.height);
			i.toWorld.mMul(src, dest);
			i.x2 = dest.getItem(0, 0);
			i.y2 = dest.getItem(0, 1);
			// Calculate the mid point of the extent of all "transformed image" extents
			i.midWX = (i.x1 + i.x2) / 2.0;
			i.midWY = (i.y1 + i.y2) / 2.0;
		}
	}
	
	private void dumpImageExtents() {
		for (AdjImage i : il) {
			System.out.println(i.imageFile + "\tW=" + i.width + "\tH=" + i.height + 
					"\tExtW=" + Math.abs(i.x2-i.x1) + "\tExtH=" + Math.abs(i.y2-i.y1));
		}
		for (AdjImage i : il) {
			System.out.println(i);
		}
	}
	
	public void doTheJob() throws IOException {
		if ( (il.size() < 2) || (il.size() >= ipl.size()))
			return;		
		
		// Assume the "first" image to be the "correct" one and orient 
		// all the others to this one
		calcUsingOriginPoint(il.get(0));
		
		// Calculate image rectangles in the world coordinate system
		calcImageExtents();
		dumpImageExtents();
		// Calculate the mid point of the extent of all "transformed image" extents
		double midx = 0.0;
		double midy = 0.0;
		for (AdjImage i : il) {
			i.midWX = (i.x1 + i.x2) / 2.0;
			i.midWY = (i.y1 + i.y2) / 2.0;
			midx += i.midWX;
			midy += i.midWY;
		}
		midx /= il.size();
		midy /= il.size();

		// Find the closest image to the mid point 
		double midDist = Double.MAX_VALUE;
		AdjImage midImage = null;
		for (AdjImage i : il) {
			double dx = i.midWX - midx;
			double dy = i.midWY - midy;
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
		double x1 = Double.MAX_VALUE;
		double y1 = Double.MAX_VALUE;
		double x2 = Double.MIN_VALUE;
		double y2 = Double.MIN_VALUE;
		for (AdjImage i : il) {
			if (x1 > i.x1) x1 = i.x1; 
			if (x1 > i.x2) x1 = i.x2; 
			if (y1 > i.y1) y1 = i.y1; 
			if (y1 > i.y2) y1 = i.y2;
			
			if (x2 < i.x1) x2 = i.x1; 
			if (x2 < i.x2) x2 = i.x2; 
			if (y2 < i.y1) y2 = i.y1; 
			if (y2 < i.y2) y2 = i.y2; 
		}
		System.out.println(x1);
		System.out.println(x2);
		System.out.println(y1);
		System.out.println(y2);
		int width = (int)(x2 - x1);
		int height = (int)(y2 - y1);
		System.out.println(width);
		System.out.println(height);
		
		
		
		
		BufferedImage bo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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
			int mask = ~(0xff << shift);
			for (int i = 0; i < width; i++)
				for (int j = 0; j < height; j++) {
					int c = bo.getRGB(i, j);
					int d = buf[0][i][j] == 0 ? 0 : buf[1][i][j] / buf[0][i][j];
					if (d < 0) d = 0;
					if (d > 255) d = 255;
					c = (c & mask) | (d << shift);
					bo.setRGB(i, j, c);
				}			
		}
		
		ImageIO.write(bo, "jpg", new File("c:/test.jpg"));
				
		// Dump results
		int count = 1;
		for (AdjImage i : il) {
			System.out.println(i.toWorld.toMatlabString("A" + count));
			System.out.println(i.fromWorld.toMatlabString("R" + count));
			count++;
		}
	}
}
