package com.slavi.img;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.tree.KDNode;
import com.slavi.tree.KDNodeSaver;
import com.slavi.tree.KDNodeSaverXML;
import com.slavi.tree.KDTree;
import com.slavi.utils.XMLHelper;

public class ScalePointList implements KDNodeSaver, KDNodeSaverXML {
	public String imageFileName = "";
	
	public int imageSizeX = 0;

	public int imageSizeY = 0;

	public KDTree kdtree = new KDTree(ScalePoint.featureVectorLinearSize);

	public void compareToList(ScalePointList dest) throws Exception {
		ArrayList points = kdtree.toList();
		ArrayList destPoints = kdtree.toList();
		
		int matchedCount1 = 0;
		for (int i = points.size() - 1; i >= 0; i--) {
			ScalePoint sp1 = (ScalePoint)points.get(i);
			boolean matchingFound = false;
			for (int j = destPoints.size() - 1; j >= 0; j--) {
				ScalePoint sp2 = (ScalePoint)destPoints.get(j);
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
			ScalePoint sp2 = (ScalePoint)destPoints.get(j);
			boolean matchingFound = false;
			for (int i = points.size() - 1; i >= 0; i--) {
				ScalePoint sp1 = (ScalePoint)points.get(i);
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
	
	public static ScalePointList fromTextStream(BufferedReader fin) throws IOException {
		ScalePointList r = new ScalePointList();
		StringTokenizer st = new StringTokenizer(fin.readLine(), "\t");
		r.imageFileName = st.nextToken();
		r.imageSizeX = Integer.parseInt(st.nextToken());
		r.imageSizeY = Integer.parseInt(st.nextToken());
		r.kdtree = KDTree.fromTextStream(ScalePoint.featureVectorLinearSize, fin, r);
		return r;
	}

	public void toTextStream(PrintWriter fou) {
		fou.println(imageFileName + "\t" + imageSizeX + "\t" + imageSizeY);
		kdtree.toTextStream(fou, this);
	}

	public KDNode nodeFromString(String source) {
		return ScalePoint.fromString(source);
	}

	public String nodeToString(KDNode node) {
		return ((ScalePoint)node).toString();
	}
	
	public KDNode nodeFromXML(Element source) throws JDOMException {
		return ScalePoint.fromXML(source);
	}

	public void nodeToXML(KDNode node, Element dest) {
		((ScalePoint)node).toXML(dest);
	}

	public void toXML(Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("imageFileName", imageFileName));
		dest.addContent(XMLHelper.makeAttrEl("imageSizeX", Integer.toString(imageSizeX)));
		dest.addContent(XMLHelper.makeAttrEl("imageSizeY", Integer.toString(imageSizeY)));
		kdtree.toXML(dest, this);
	}
	
	public static ScalePointList fromXML(Element source) throws JDOMException {
		ScalePointList r = new ScalePointList();
		r.imageFileName = XMLHelper.getAttrEl(source, "imageFileName");
		r.imageSizeX = Integer.parseInt(XMLHelper.getAttrEl(source, "imageSizeX"));
		r.imageSizeY = Integer.parseInt(XMLHelper.getAttrEl(source, "imageSizeY"));
		r.kdtree = KDTree.fromXML(source, r);
		return r;			
	}
}
