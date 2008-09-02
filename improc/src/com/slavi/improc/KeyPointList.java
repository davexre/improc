package com.slavi.improc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FileStamp;
import com.slavi.util.tree.KDTree;

public class KeyPointList extends KDTree<KeyPoint> {
	public static final String fileHeader = "KeyPoint file version 1.2";
	
	public FileStamp imageFileStamp = null;
	
	public int imageSizeX;

	public int imageSizeY;
	
	public KeyPointList() {
		super(KeyPoint.featureVectorLinearSize);
	}

	public boolean canFindDistanceBetween(KeyPoint fromNode, KeyPoint toNode) {
		return fromNode != toNode;
	}

	public double getValue(KeyPoint node, int dimensionIndex) {
		return node.getValue(dimensionIndex);
	}
	
	public void add(KeyPoint item) {
		item.keyPointList = this;
		super.add(item);
	}
	
	public static KeyPointList fromTextStream(BufferedReader fin, AbsoluteToRelativePathMaker rootImagesDir) throws IOException {
		KeyPointList r = new KeyPointList();
		r.imageFileStamp = FileStamp.fromString(fin.readLine(), rootImagesDir);
		StringTokenizer st = new StringTokenizer(fin.readLine(), "\t");
		r.imageSizeX = Integer.parseInt(st.nextToken());
		r.imageSizeY = Integer.parseInt(st.nextToken());
		new KeyPointListSaver(r).fromTextStream(r, fin);
		return r;
	}

	public void toTextStream(PrintWriter fou) {
		fou.println(imageFileStamp.toString());
		fou.println(imageSizeX + "\t" + imageSizeY);
		new KeyPointListSaver(this).toTextStream(this, fou);
	}

	public HashMap<Integer, KeyPoint> makeMap() {
		HashMap<Integer, KeyPoint> result = new HashMap<Integer, KeyPoint>(getSize());
		for (KeyPoint i : this) {
			result.put(i.id, i);
		}
		return result;
	}
	
	public void compareToList(KeyPointList dest) {
		ArrayList<KeyPoint> points = toList();
		ArrayList<KeyPoint> destPoints = dest.toList();
		
		int matchedCount1 = 0;
		for (int i = points.size() - 1; i >= 0; i--) {
			KeyPoint sp1 = points.get(i);
			boolean matchingFound = false;
			for (int j = destPoints.size() - 1; j >= 0; j--) {
				KeyPoint sp2 = destPoints.get(j);
				if (sp1.equals(sp2)) {
					matchingFound = true;
					matchedCount1++;
					break;
				}
			}
			if (!matchingFound)
				System.out.println("Point No " + i + " from 1-st list has no match in 2-nd list");
		}

		int matchedCount2 = 0;
		for (int j = destPoints.size() - 1; j >= 0; j--) {
			KeyPoint sp2 = destPoints.get(j);
			boolean matchingFound = false;
			for (int i = points.size() - 1; i >= 0; i--) {
				KeyPoint sp1 = points.get(i);
				if (sp1.equals(sp2)) {
					matchingFound = true;
					matchedCount2++;
					break;
				}
			}
			if (!matchingFound)
				System.out.println("Point No " + j + " from 2-nd list has no match in 1-st list");
		}
		
		System.out.println("Matched 1-st list against 2-nd list: " + matchedCount1 + "/" + points.size());
		System.out.println("Matched 2-nd list against 1-st list: " + matchedCount2 + "/" + destPoints.size());
	}
}
