package com.slavi.img;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.slavi.statistics.AffineTransformLearner;
import com.slavi.utils.AbsoluteToRelativePathMaker;
import com.slavi.utils.FileStamp;

public class PanoPairList {
	public static final String fileHeader = "PanoPair file version 1.2";

	public ArrayList<PanoPair> items;

	public FileStamp sourceKPL = null;

	public FileStamp targetKPL = null;

	public File sourceImage;
	public File targetImage;
	
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
			File image1, File image2) throws IOException {
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
			File image1, File image2) throws IOException {
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
		result.targetImageSizeX = kppl.target.imageSizeY;
		result.items = new ArrayList<PanoPair>();

		AffineTransformLearner atl = new AffineTransformLearner(2, 2, kppl.items);
		atl.calculateOne();
		atl.calculateOne();
		atl.calculateOne();
		atl.calculateOne();
		kppl.leaveGoodElements(9.0); // Math.min(image.sizex, image.sizeY) * 0.005; // 0.5% of the size
		atl.calculateOne();
		atl.calculateOne();
		for (int i = 0; i < kppl.items.size(); i++) {
			KeyPointPair pp = (KeyPointPair) kppl.items.get(i);
			if (pp.discrepancy < 2.0) {
				result.items.add(new PanoPair(pp));
			}				
		}
		
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
		
		for (PanoPair i : result.items) {
			fou.println(i.toString());
		}
		if (fou.checkError()) {
			fou.close();
			throw new IOException("Write to file failed.");
		}
		fou.close();
		
		result.sourceImage = image1;
		result.targetImage = image2;
		return result;
	}

	public static PanoPairList readKeyPointPairFile(
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			AbsoluteToRelativePathMaker rootKeyPointPairFileDir,
			File image1, File image2) throws IOException {
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

		while (fin.ready()) {
			String str = fin.readLine().trim();
			if ((str.length() > 0) && (str.charAt(0) != '#')) {
				result.items.add(PanoPair.fromString(str));
			}
		}

		result.sourceImage = image1;
		result.targetImage = image2;
		return result;
	}

	public void writeToPtoFile(File ptoFile) throws FileNotFoundException {
		int goodCount = 0;
		for (int i = 0; i < items.size(); i++) {
			PanoPair pp = items.get(i);
			if (pp.discrepancy < 2.0)
				goodCount++;
		}
		if (goodCount < 10) {
			ptoFile.delete();
			return;
		}
		
		PrintStream fou = new PrintStream(ptoFile);
		fou.println("# Hugin project file");
		fou.println("# automatically generated by autopano-sift, available at");
		fou.println("p f2 w3000 h1500 v360  n\"JPEG q90\"");
		fou.println("m g1 i0\n");
		//...
		//pto.WriteLine ("i w{0} h{1} f0 a={2} b={2} c={2} d0 e0 p{3} r{4} v={2} y{5}  u10 n\"{6}\"",
		//   fou.println("i w{0} h{1} f0 a0 b-0.01 c0 d0 e0 p{2} r{3} v180 y{4}  u10 n\"{5}\"");
		//kx.XDim, kx.YDim, refIdx, pitch, rotation, yaw, imageFile
		fou.println("i w" + sourceImageSizeX + " h" + sourceImageSizeY + " f0 a0 b0 c0 d0 e0 g0 p0 r0 t0 v38 y0 u10 n\"" + sourceImage.getPath() + "\"");
		fou.println("i w" + targetImageSizeX + " h" + targetImageSizeY + " f0 a0 b0 c0 d0 e0 g0 p0 r0 t0 v38 y0 u10 n\"" + targetImage.getPath() + "\"");
		
		fou.println();
		fou.println("v p1 r1 y1");
		fou.println();

		int pointCounter = 0;		
		for (int i = 0; i < items.size(); i++) {
			PanoPair pp = items.get(i);
			if (pp.discrepancy < 2.0) {
				pointCounter++;
				//fou.println("c n{0} N{1} x{2} y{3} X{4} Y{5} t0");
				//imageNameTab[ms.File1], imageNameTab[ms.File2],m.Kp1.X, m.Kp1.Y, m.Kp2.X, m.Kp2.Y
				fou.println("c n0 N1" +
					" x" + Double.toString(pp.sx) + 
					" y" + Double.toString(pp.sy) + 
					" X" + Double.toString(pp.tx) + 
					" Y" + Double.toString(pp.ty) + " t0");
			}
		}
		fou.println();
		fou.println("# match list automatically generated");
	}
}
