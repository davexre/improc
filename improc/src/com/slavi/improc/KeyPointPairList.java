package com.slavi.improc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FileStamp;
import com.slavi.util.tree.KDTree;

public class KeyPointPairList {
	public static final String fileHeader = "KeyPointPair file version 1.2";

	public ArrayList<KeyPointPair> items;
	
	public KeyPointList source;

	public KeyPointList target;
	
	public FileStamp sourceKPL = null;

	public FileStamp targetKPL = null;

	private KeyPointPairList() {
		items = new ArrayList<KeyPointPair>();
	}

	public static void updateKeyPointPairFileIfNecessary(
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			AbsoluteToRelativePathMaker rootKeyPointPairFileDir,
			File image1, File image2) throws Exception {
		doUpdateKeyPointPairList(rootImagesDir, rootKeyPointFileDir, rootKeyPointPairFileDir, image1, image2);
	}

	public static File getFile(AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointPairFileDir,
			File image1, File image2) {
		if (image1.getName().compareTo(image2.getName()) > 0) {
			File tmp = image1;
			image1 = image2;
			image2 = tmp;
		}
		return new File(
			rootKeyPointPairFileDir.getFullPath(rootImagesDir.getRelativePath(image1, false)) +
			"-" + image2.getName() + ".kppl");
	}
	
	private static KeyPointPairList doUpdateKeyPointPairList(
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			AbsoluteToRelativePathMaker rootKeyPointPairFileDir,
			File image1, File image2) throws Exception {
		if (image1.getName().compareTo(image2.getName()) > 0) {
			File tmp = image1;
			image1 = image2;
			image2 = tmp;
		}
		KeyPointListSaver.updateKeyPointFileIfNecessary(rootImagesDir, rootKeyPointFileDir, image1);
		KeyPointListSaver.updateKeyPointFileIfNecessary(rootImagesDir, rootKeyPointFileDir, image2);
		
		File kppFile = getFile(rootImagesDir, rootKeyPointPairFileDir, image1, image2);
		try {
			if (kppFile.isFile()) {
				BufferedReader fin = new BufferedReader(new FileReader(kppFile));
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
		
		// Build the KeyPointPairList file
		KeyPointPairList result = new KeyPointPairList();
		
		result.source = KeyPointListSaver.readKeyPointFile(rootImagesDir, rootKeyPointFileDir, image1);
		result.target = KeyPointListSaver.readKeyPointFile(rootImagesDir, rootKeyPointFileDir, image2);
		result.sourceKPL = new FileStamp(rootKeyPointFileDir.getRelativePath(KeyPointListSaver.getFile(rootImagesDir, rootKeyPointFileDir, image1) ), rootKeyPointFileDir);
		result.targetKPL = new FileStamp(rootKeyPointFileDir.getRelativePath(KeyPointListSaver.getFile(rootImagesDir, rootKeyPointFileDir, image2) ), rootKeyPointFileDir);
		int searchSteps = (int) (Math.max(130.0, (Math.log(result.source.getSize()) / Math.log (1000.0)) * 130.0));

		for (KeyPoint p1 : result.source) {
			KDTree.NearestNeighbours<KeyPoint> nnlst = result.target.getNearestNeighboursBBF(p1, 2, searchSteps);
			if (nnlst.size() < 2)
				continue;
			if (nnlst.getDistanceToTarget(0) > nnlst.getDistanceToTarget(1) * 0.6) {
				continue;
			}
			KeyPoint p2 = nnlst.getItem(0);
			KeyPointPair kpp = new KeyPointPair(p1, p2, nnlst.getDistanceToTarget(0), nnlst.getDistanceToTarget(1));
			for (KeyPointPair i : result.items) 
				if (i.targetSP == p2) {
					kpp.targetReused = true;
					break;
				}
			result.items.add(kpp);
		}
		
		// The point pair list is built. Now save it.
		kppFile.getParentFile().mkdirs();
		PrintWriter fou = new PrintWriter(kppFile);
		fou.println(fileHeader);
		fou.println(result.sourceKPL.toString());
		fou.println(result.targetKPL.toString());
		
		for (KeyPointPair i : result.items) {
			fou.println(
				Double.toString(i.distanceToNearest) + " \t" +
				Double.toString(i.distanceToNearest2) + " \t" +
				Boolean.toString(i.targetReused) + " \t" +
				Integer.toString(i.sourceSP.id) + "\t" +
				Integer.toString(i.targetSP.id));
		}
		if (fou.checkError()) {
			fou.close();
			throw new IOException("Write to file failed.");
		}
		fou.close();
		return result;
	}

	public static KeyPointPairList readKeyPointPairFile(
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			AbsoluteToRelativePathMaker rootKeyPointPairFileDir,
			File image1, File image2) throws Exception {
		KeyPointPairList result = doUpdateKeyPointPairList(rootImagesDir, rootKeyPointFileDir, rootKeyPointPairFileDir, image1, image2);
		if (result != null)
			return result;
				
		File kppFile = getFile(rootImagesDir, rootKeyPointPairFileDir, image1, image2);
		BufferedReader fin = new BufferedReader(new FileReader(kppFile));
		fin.readLine(); // Skip header.
		result = new KeyPointPairList();
		result.source = KeyPointListSaver.readKeyPointFile(rootImagesDir, rootKeyPointFileDir, image1);
		result.target = KeyPointListSaver.readKeyPointFile(rootImagesDir, rootKeyPointFileDir, image2);
		result.sourceKPL = FileStamp.fromString(fin.readLine(), rootKeyPointFileDir);
		result.targetKPL = FileStamp.fromString(fin.readLine(), rootKeyPointFileDir);
		result.items = new ArrayList<KeyPointPair>();
		HashMap<Integer, KeyPoint> sourceMap = result.source.makeMap();
		HashMap<Integer, KeyPoint> targetMap = result.target.makeMap();

		while (fin.ready()) {
			String str = fin.readLine().trim();
			if ((str.length() > 0) && (str.charAt(0) != '#')) {
				StringTokenizer st = new StringTokenizer(str, "\t");
				double distanceToNearest = Double.parseDouble(st.nextToken());
				double distanceToNearest2 = Double.parseDouble(st.nextToken());
				boolean targetReused = Boolean.getBoolean(st.nextToken());
				int sourcePointId = Integer.parseInt(st.nextToken());
				int targetPointId = Integer.parseInt(st.nextToken());
				KeyPoint sourceSP = sourceMap.get(sourcePointId);
				KeyPoint targetSP = targetMap.get(targetPointId);
				KeyPointPair kpp = new KeyPointPair(sourceSP, targetSP, distanceToNearest, distanceToNearest2);
				kpp.targetReused = targetReused;
				result.items.add(kpp);
			}
		}
		return result;
	}
	
	// TODO: OK up to here

	public int countGoodItems() {
		int r = 0;
		for (int i = items.size() - 1; i >= 0; i--)
			if (!items.get(i).isBad())
				r++;
		return r;
	}
	
	public int leaveGoodTopElements(int numElements) {
		int count = 0;
		for (int i = 0; i < items.size(); i++) {
			KeyPointPair sp = items.get(i);
			if (count >= numElements) { 
				sp.setBad(true);
			} else {
				sp.setBad(false);
				count++;
			}
		}
		return count;
	}
	
	public int leaveGoodTopElements2(int numElements) {
		int count = 0;
		for (int i = 0; i < items.size(); i++) {
			KeyPointPair sp = items.get(i);
			if (sp.targetReused || (count >= numElements)) { 
				sp.setBad(true);
			} else {
				sp.setBad(false);
				count++;
			}
		}
		return count;
	}
	
	public void leaveGoodElements(double maxDiscrepancy) {
		for (int i = 0; i < items.size(); i++) {
			KeyPointPair sp = items.get(i);
			sp.setBad(sp.discrepancy > maxDiscrepancy);
		}
	}
	
	private static class CompareByDistance implements Comparator<KeyPointPair> {
		public static final CompareByDistance instance = new CompareByDistance();

		public int compare(KeyPointPair spp1, KeyPointPair spp2) {
			return Double.compare(spp1.distanceToNearest, spp2.distanceToNearest);
		} 
	}
	public void sortByDistance() {
		Collections.sort(items, CompareByDistance.instance);
	}
	
	private static class CompareByOverallFitness implements Comparator<KeyPointPair> {
		public static final CompareByOverallFitness instance = new CompareByOverallFitness();

		public int compare(KeyPointPair spp1, KeyPointPair spp2) {
			return Double.compare(spp1.overallFitness, spp2.overallFitness);
		} 
	}
	public void sortByOverallFitness() {
		Collections.sort(items, CompareByOverallFitness.instance);
	}
	
	private static class CompareByDiscrepancy implements Comparator<KeyPointPair> {
		public static final CompareByDiscrepancy instance = new CompareByDiscrepancy();

		public int compare(KeyPointPair spp1, KeyPointPair spp2) {
			return Double.compare(spp1.discrepancy, spp2.discrepancy);
		} 
	}
	public void sortByDiscrepancy() {
		Collections.sort(items, CompareByDiscrepancy.instance);
	}		

	private static class CompareByWeight implements Comparator<KeyPointPair> {
		public static final CompareByWeight instance = new CompareByWeight();

		public int compare(KeyPointPair spp1, KeyPointPair spp2) {
			return Double.compare(spp2.getWeight(), spp1.getWeight());  // Weight comparison is DESCENDING
		} 
	}
	public void sortByWeight() {
		Collections.sort(items, CompareByWeight.instance);
	}

	protected static double fixAnglePI(double angle) {
		return Math.abs(angle - Math.floor(angle / Math.PI) * Math.PI);
	}
	
	private static class CompareByOrientationDelta implements Comparator<KeyPointPair> {
		public static final CompareByOrientationDelta instance = new CompareByOrientationDelta();

		public int compare(KeyPointPair spp1, KeyPointPair spp2) {
			return Double.compare(
				fixAnglePI(spp1.sourceSP.degree - spp1.targetSP.degree), 
				fixAnglePI(spp2.sourceSP.degree - spp2.targetSP.degree));
		} 
	}
	
	public void sortByOrientationDelta() {
		Collections.sort(items, CompareByOrientationDelta.instance);
	}
	
	public void displayTop(int maxTop) {
		maxTop = (maxTop <= 0 ? items.size() : Math.min(maxTop, items.size()));
		for (int i = 0; i < maxTop; i++) {
			KeyPointPair pp = items.get(i);
			System.out.println((i+1) + " -> " + (pp.isBad() ? "BAD " : "ok  ") + pp.discrepancy);
		}
	}
}
