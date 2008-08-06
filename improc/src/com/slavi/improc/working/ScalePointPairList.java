package com.slavi.improc.working;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.util.XMLHelper;

public class ScalePointPairList {

	public ArrayList items;

	String sourceImageFileName;
	int sourceImageSizeX;
	int sourceImageSizeY;

	String targetImageFileName;
	int targetImageSizeX;
	int targetImageSizeY;
	
	private ScalePointPairList() {
		items = new ArrayList();
	}

	public ScalePointPairList(ScalePointList source, ScalePointList target) {
		items = new ArrayList();
		sourceImageFileName = source.imageFileName;
		sourceImageSizeX = source.imageSizeX;
		sourceImageSizeY = source.imageSizeY;
		
		targetImageFileName = target.imageFileName;
		targetImageSizeX = target.imageSizeX;
		targetImageSizeY = target.imageSizeY;
	}
	
	public void addPair(ScalePoint sourceSP, ScalePoint targetSP, double distanceToNearest, double distanceToNearest2) {
		ScalePointPair sp = new ScalePointPair(sourceSP, targetSP, distanceToNearest, distanceToNearest2);
		for (int i = items.size() - 1; i >= 0; i--) {
			ScalePointPair aSP = (ScalePointPair) items.get(i);
			if (aSP.targetSP == sp.targetSP) {
				aSP.targetReused = true;
				sp.targetReused = true;
				break;
			}
		}
		items.add(sp);
	}
	
	public void toXML(Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("sourceImageFileName", sourceImageFileName));
		dest.addContent(XMLHelper.makeAttrEl("sourceImageSizeX", Integer.toString(sourceImageSizeX)));
		dest.addContent(XMLHelper.makeAttrEl("sourceImageSizeY", Integer.toString(sourceImageSizeY)));

		dest.addContent(XMLHelper.makeAttrEl("targetImageFileName", targetImageFileName));
		dest.addContent(XMLHelper.makeAttrEl("targetImageSizeX", Integer.toString(targetImageSizeX)));
		dest.addContent(XMLHelper.makeAttrEl("targetImageSizeY", Integer.toString(targetImageSizeY)));
		
		for (int i = 0; i < items.size(); i++) {
			Element e = new Element("sppair");
			((ScalePointPair) items.get(i)).toXML(e);
			dest.addContent(e);
		}
	}
	
	public static ScalePointPairList fromXML(Element source) throws JDOMException {
		ScalePointPairList r = new ScalePointPairList();
		r.sourceImageFileName = XMLHelper.getAttrEl(source, "sourceImageFileName");
		r.sourceImageSizeX = Integer.parseInt(XMLHelper.getAttrEl(source, "sourceImageSizeX"));
		r.sourceImageSizeY = Integer.parseInt(XMLHelper.getAttrEl(source, "sourceImageSizeY"));

		r.targetImageFileName = XMLHelper.getAttrEl(source, "targetImageFileName");
		r.targetImageSizeX = Integer.parseInt(XMLHelper.getAttrEl(source, "targetImageSizeX"));
		r.targetImageSizeY = Integer.parseInt(XMLHelper.getAttrEl(source, "targetImageSizeY"));
		
		List pairs = source.getChildren("sppair");
		for (int i = 0; i < pairs.size(); i++) {
			Element e = (Element) pairs.get(i);
			ScalePointPair pp = ScalePointPair.fromXML(e);
			pp.id = i;
			r.items.add(pp);
		}
		return r;
	}
	
	public int countGoodItems() {
		int r = 0;
		for (int i = items.size() - 1; i >= 0; i--)
			if (!((ScalePointPair) items.get(i)).isBad())
				r++;
		return r;
	}
	
	public int leaveGoodTopElements(int numElements) {
		int count = 0;
		for (int i = 0; i < items.size(); i++) {
			ScalePointPair sp = (ScalePointPair)items.get(i);
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
			ScalePointPair sp = (ScalePointPair)items.get(i);
			if (sp.targetReused || (count >= numElements)) { 
				sp.setBad(true);
			} else {
				sp.setBad(false);
				count++;
			}
		}
		return count;
	}
	
	private static class CompareByDistance implements Comparator {
		public static final CompareByDistance instance = new CompareByDistance();

		public int compare(Object o1, Object o2) {
			ScalePointPair spp1 = (ScalePointPair)o1;
			ScalePointPair spp2 = (ScalePointPair)o2;
			return Double.compare(spp1.distanceToNearest, spp2.distanceToNearest);
		} 
	}
	public void sortByDistance() {
		Collections.sort(items, CompareByDistance.instance);
	}
	
	private static class CompareByOverallFitness implements Comparator {
		public static final CompareByOverallFitness instance = new CompareByOverallFitness();

		public int compare(Object o1, Object o2) {
			ScalePointPair spp1 = (ScalePointPair)o1;
			ScalePointPair spp2 = (ScalePointPair)o2;
			return Double.compare(spp1.overallFitness, spp2.overallFitness);
		} 
	}
	public void sortByOverallFitness() {
		Collections.sort(items, CompareByOverallFitness.instance);
	}
	
	private static class CompareByDelta implements Comparator {
		public static final CompareByDelta instance = new CompareByDelta();

		public int compare(Object o1, Object o2) {
			ScalePointPair spp1 = (ScalePointPair)o1;
			ScalePointPair spp2 = (ScalePointPair)o2;
			return Double.compare(spp1.getValue(), spp2.getValue());
		} 
	}
	public void sortByDelta() {
		Collections.sort(items, CompareByDelta.instance);
	}		

	private static class CompareByWeight implements Comparator {
		public static final CompareByWeight instance = new CompareByWeight();

		public int compare(Object o1, Object o2) {
			ScalePointPair spp1 = (ScalePointPair)o1;
			ScalePointPair spp2 = (ScalePointPair)o2;
			return Double.compare(spp2.getWeight(), spp1.getWeight());  // Weight comparison is DESCENDING
		} 
	}
	public void sortByWeight() {
		Collections.sort(items, CompareByWeight.instance);
	}

	protected static double fixAnglePI(double angle) {
		return Math.abs(angle - Math.floor(angle / Math.PI) * Math.PI);
	}
	
	private static class CompareByOrientationDelta implements Comparator {
		public static final CompareByOrientationDelta instance = new CompareByOrientationDelta();

		public int compare(Object o1, Object o2) {
			ScalePointPair spp1 = (ScalePointPair)o1;
			ScalePointPair spp2 = (ScalePointPair)o2;
			return Double.compare(
				fixAnglePI(spp1.sourceSP.degree - spp1.targetSP.degree), 
				fixAnglePI(spp2.sourceSP.degree - spp2.targetSP.degree));
		} 
	}
	public void sortByOrientationDelta() {
		Collections.sort(items, CompareByOrientationDelta.instance);
	}
}
