package com.slavi.improc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.slavi.math.transform.AffineTransformLearner;
import com.slavi.math.transform.AffineTransformer;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FileStamp;

public class PanoPairList {
	public static final String fileHeader = "PanoPair file version 1.2.1";

	public ArrayList<PanoPair> items;
	
	public AffineTransformer transform = new AffineTransformer(2, 2);
	
	public FileStamp sourceKPL = null;

	public FileStamp targetKPL = null;

	public String sourceImage;
	
	public String targetImage;
	
	public int sourceImageSizeX;
	
	public int sourceImageSizeY;
	
	public int targetImageSizeX;
	
	public int targetImageSizeY; 
	
	private PanoPairList() {
		items = new ArrayList<PanoPair>();
	}

	public static void updatePanoPairFileIfNecessary(
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			AbsoluteToRelativePathMaker rootKeyPointPairFileDir,
			File image1, File image2) throws Exception {
		doUpdatePanoPairList(rootImagesDir, rootKeyPointFileDir, rootKeyPointPairFileDir, image1, image2);
	}

	public static File getFile(AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			AbsoluteToRelativePathMaker rootKeyPointPairFileDir,
			File image1, File image2) {
		if (image1.getName().compareTo(image2.getName()) > 0) {
			File tmp = image1;
			image1 = image2;
			image2 = tmp;
		}
		return new File(
			rootKeyPointPairFileDir.getFullPath(rootImagesDir.getRelativePath(image1, false)) +
			"-" + image2.getName() + ".pano");
	}
	
	private static PanoPairList doUpdatePanoPairList(
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			AbsoluteToRelativePathMaker rootKeyPointPairFileDir,
			File image1, File image2) throws Exception {
		if (image1.getName().compareTo(image2.getName()) > 0) {
			File tmp = image1;
			image1 = image2;
			image2 = tmp;
		}
		KeyPointList.updateKeyPointFileIfNecessary(rootImagesDir, rootKeyPointFileDir, image1);
		KeyPointList.updateKeyPointFileIfNecessary(rootImagesDir, rootKeyPointFileDir, image2);
		
		File panoFile = getFile(rootImagesDir, rootKeyPointFileDir, rootKeyPointPairFileDir, image1, image2);

		try {
			if (panoFile.isFile()) {
				BufferedReader fin = new BufferedReader(new FileReader(panoFile));
				if (fileHeader.equals(fin.readLine())) {
					FileStamp fs1 = FileStamp.fromString(fin.readLine(), rootKeyPointFileDir);
					FileStamp fs2 = FileStamp.fromString(fin.readLine(), rootKeyPointFileDir);
					if ( (!fs1.isModified()) && (!fs2.isModified()) ) {
						// The KeyPointList Files are not modified, so don't build
						return null;
					}
				}
			}
		} catch (IOException e) {
		}
	
		// Build the PanoPairList file
		KeyPointPairList kppl = KeyPointPairList.readKeyPointPairFile(rootImagesDir, rootKeyPointFileDir, rootKeyPointPairFileDir, image1, image2);
		PanoPairList result = new PanoPairList();
		result.sourceKPL = new FileStamp(rootKeyPointFileDir.getRelativePath(KeyPointList.getFile(rootImagesDir, rootKeyPointFileDir, image1) ), rootKeyPointFileDir);
		result.targetKPL = new FileStamp(rootKeyPointFileDir.getRelativePath(KeyPointList.getFile(rootImagesDir, rootKeyPointFileDir, image2) ), rootKeyPointFileDir);
		result.sourceImageSizeX = kppl.source.imageSizeX;
		result.sourceImageSizeY = kppl.source.imageSizeY;
		result.targetImageSizeX = kppl.target.imageSizeX;
		result.targetImageSizeY = kppl.target.imageSizeY;
		result.items = new ArrayList<PanoPair>();

		AffineTransformLearner atl = new AffineTransformLearner(2, 2, kppl.items);
		atl.calculateOne();
		atl.calculateOne();
		atl.calculateOne();
		atl.calculateOne();
		kppl.leaveGoodElements(9.0); // Math.min(image.sizex, image.sizeY) * 0.005; // 0.5% of the size
		atl.calculateOne();
		atl.calculateOne();
		
//		System.out.println("*** after calc 6");
//		kppl.sortByDiscrepancy();
//		kppl.displayTop(30);
		
		for (int i = 0; i < kppl.items.size(); i++) {
			KeyPointPair pp = (KeyPointPair) kppl.items.get(i);
			if (pp.getValue() < 2.0) {
				result.items.add(new PanoPair(pp));
			}				
		}
		result.transform = (AffineTransformer) atl.transformer;

		// The PanoPairList is built. Now save it.
		panoFile.getParentFile().mkdirs();
		PrintWriter fou = new PrintWriter(panoFile);
		fou.println(fileHeader);
		fou.println(result.sourceKPL.toString());
		fou.println(result.targetKPL.toString());
		fou.println(
			Integer.toString(result.sourceImageSizeX) + "\t" +
			Integer.toString(result.sourceImageSizeY) + "\t" +
			Integer.toString(result.targetImageSizeX) + "\t" +
			Integer.toString(result.targetImageSizeY));
		double d[] = new double[6];
		result.transform.getMatrix(d);
		String prefix = "";
		for (int i = 0; i < d.length; i++) {
			fou.print(prefix);
			fou.print(Double.toString(d[i]));
			prefix = "\t";
		}
		fou.println();
		
		for (PanoPair i : result.items) {
			fou.println(i.toString());
		}
		if (fou.checkError()) {
			fou.close();
			throw new IOException("Write to file failed.");
		}
		fou.close();
		
		result.sourceImage = image1.getAbsolutePath();
		result.targetImage = image2.getAbsolutePath();
		return result;
	}

	public static PanoPairList readPanoPairFile(
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			AbsoluteToRelativePathMaker rootKeyPointPairFileDir,
			File image1, File image2) throws Exception {
		PanoPairList result = doUpdatePanoPairList(rootImagesDir, rootKeyPointFileDir, rootKeyPointPairFileDir, image1, image2);
		if (result != null)
			return result;
				
		File kppFile = getFile(rootImagesDir, rootKeyPointFileDir, rootKeyPointPairFileDir, image1, image2);
		BufferedReader fin = new BufferedReader(new FileReader(kppFile));
		fin.readLine(); // Skip header.
		result = new PanoPairList();
		result.sourceKPL = FileStamp.fromString(fin.readLine(), rootKeyPointFileDir);
		result.targetKPL = FileStamp.fromString(fin.readLine(), rootKeyPointFileDir);
		StringTokenizer st = new StringTokenizer(fin.readLine(), "\t");
		result.sourceImageSizeX = Integer.parseInt(st.nextToken());
		result.sourceImageSizeY = Integer.parseInt(st.nextToken());
		result.targetImageSizeX = Integer.parseInt(st.nextToken());
		result.targetImageSizeY = Integer.parseInt(st.nextToken());
		st = new StringTokenizer(fin.readLine(), "\t");
		double d[] = new double[6];
		for (int i = 0; i < d.length; i++)
			d[i] = Double.parseDouble(st.nextToken());
		result.transform.setMatrix(d);
		
		while (fin.ready()) {
			String str = fin.readLine().trim();
			if ((str.length() > 0) && (str.charAt(0) != '#')) {
				result.items.add(PanoPair.fromString(str));
			}
		}

		result.sourceImage = image1.getAbsolutePath();
		result.targetImage = image2.getAbsolutePath();
		return result;
	}
}
