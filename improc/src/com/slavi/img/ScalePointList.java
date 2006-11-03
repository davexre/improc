package com.slavi.img;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.utils.XMLHelper;

public class ScalePointList {
	public String imageFileName = "";
	
	public int imageSizeX = 0;

	public int imageSizeY = 0;

	public ArrayList points;
	
	public KDTree kdtree = null;

	public ScalePointList() {
		points = new ArrayList();
	}
	
	public void buildTree() {
		kdtree = new KDTree(points, ScalePoint.featureVectorLinearSize); 
	}
	
	public void compareToList(ScalePointList dest) throws Exception {
		int matchedCount1 = 0;
		for (int i = points.size() - 1; i >= 0; i--) {
			ScalePoint sp1 = (ScalePoint)points.get(i);
			boolean matchingFound = false;
			for (int j = dest.points.size() - 1; j >= 0; j--) {
				ScalePoint sp2 = (ScalePoint)dest.points.get(j);
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
		for (int j = dest.points.size() - 1; j >= 0; j--) {
			ScalePoint sp2 = (ScalePoint)dest.points.get(j);
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
		System.out.println("Matched 2-nd list against 1-st list: " + matchedCount2 + "/" + dest.points.size());
		System.out.println("Finished!");
	}
	
	public static ScalePointList fromTextStream(BufferedReader fin) throws IOException {
		ScalePointList r = new ScalePointList();
		StringTokenizer st = new StringTokenizer(fin.readLine(), "\t");
		r.imageFileName = st.nextToken();
		r.imageSizeX = Integer.parseInt(st.nextToken());
		r.imageSizeY = Integer.parseInt(st.nextToken());
		while (fin.ready()) {
			String str = fin.readLine().trim();
			if ((str.length() > 0) && (str.charAt(0) != '#'))
				r.points.add(ScalePoint.fromString(str));
		}
		return r;
	}

	public void toTextStream(PrintWriter fou) {
		fou.println(imageFileName + "\t" + imageSizeX + "\t" + imageSizeY);
		for (int i = 0; i < points.size(); i++)
			fou.println(((ScalePoint)points.get(i)).toString());
	}
	
	public void toXML(Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("imageFileName", imageFileName));
		dest.addContent(XMLHelper.makeAttrEl("imageSizeX", Integer.toString(imageSizeX)));
		dest.addContent(XMLHelper.makeAttrEl("imageSizeY", Integer.toString(imageSizeY)));
		for (int i = 0; i < points.size(); i++) {
			Element e = new Element("ScalePoint");
			((ScalePoint)points.get(i)).toXML(e);
			dest.addContent(e);
		}
	}
	
	public static ScalePointList fromXML(Element source) throws JDOMException {
		ScalePointList r = new ScalePointList();
		r.imageFileName = XMLHelper.getAttrEl(source, "imageFileName");
		r.imageSizeX = Integer.parseInt(XMLHelper.getAttrEl(source, "imageSizeX"));
		r.imageSizeY = Integer.parseInt(XMLHelper.getAttrEl(source, "imageSizeY"));
		List list = source.getChildren("ScalePoint");
		for (int i = 0; i < list.size(); i++) {
			ScalePoint sp = ScalePoint.fromXML((Element)list.get(i));
			sp.scalePointList = r;
			r.points.add(sp);
		}
		r.buildTree();
		return r;			
	}
}
