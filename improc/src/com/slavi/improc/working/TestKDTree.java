package com.slavi.improc.working;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.util.XMLHelper;

public class TestKDTree {

	public static ScalePointList loadAutoPanoFile(String finName) throws JDOMException, IOException {
		ScalePointList r = new ScalePointList();
		Element root = XMLHelper.readXML(new File(finName));
		r.imageFileName = root.getChildText("ImageFile");
		r.imageSizeX = Integer.parseInt(root.getChildText("XDim"));
		r.imageSizeY = Integer.parseInt(root.getChildText("YDim"));
		
		List kpl = root.getChild("Arr").getChildren("KeypointN");
		
		for (int counter = 0; counter < kpl.size(); counter++) {
			Element key = (Element)kpl.get(counter);
			ScalePoint sp = new ScalePoint();
			sp.doubleX = Double.parseDouble(key.getChildText("X"));
			sp.doubleY = Double.parseDouble(key.getChildText("Y"));
			sp.imgX = (int)sp.doubleX;
			sp.imgY = (int)sp.doubleY;
			sp.adjS = 0;
			sp.degree = Double.parseDouble(key.getChildText("Orientation"));
			sp.level = Integer.parseInt(key.getChildText("Level"));
			sp.kpScale = Double.parseDouble(key.getChildText("Scale"));
			sp.scalePointList = r;
			
			List descr = key.getChild("Descriptor").getChildren("int");
			for (int i = 0; i < ScalePoint.descriptorSize; i++) {
				for (int j = 0; j < ScalePoint.descriptorSize; j++) {
					for (int k = 0; k < ScalePoint.numDirections; k++) {
						int index = 
							i * ScalePoint.descriptorSize * ScalePoint.numDirections +
							j * ScalePoint.numDirections + k;
						sp.setItem(i, j, k, Double.parseDouble(
							((Element)descr.get(index)).getText()));
					}
				}
			}
			r.points.add(sp);
		}
		return r;
	}

	public static void buildPointPairList(ScalePointList a, ScalePointList b)  {
//		int denied = 0;
//		if (a.points.size() > b.points.size()) {
//			ScalePointList tmp = a;
//			a = b;
//			b = tmp;
//		}		
		int searchSteps = (int) (Math.max(130.0, (Math.log(a.points.size()) / Math.log (1000.0)) * 130.0));
		
		for (int p = 0; p < a.points.size(); p++) {
			ScalePoint ap = (ScalePoint) a.points.get(p);
			//NearestNeighbours nnlst =
			b.kdtree.getNearestNeighboursBBF(ap, 2, searchSteps);
//			if (nnlst.size() < 2)
//				continue;
//			if (nnlst.getValue(0) > nnlst.getValue(1) * 0.6) {
//				denied++;
//				continue;
//			}
			fou.println("" + p + "\t" + b.kdtree.usedSearchSteps + "\t" + searchSteps);
		}
	}
	
	public static PrintWriter fou;

	public static void main(String[] args) throws Exception {
		//fou = new PrintWriter("./../../images/debug.my");
		ScalePointList a1 = loadAutoPanoFile("./../../images/a1.key");
		ScalePointList a2 = loadAutoPanoFile("./../../images/a2.key");
//		a1.kdtree.balance();
//		a2.kdtree.balance();
		System.out.println("a1.points.size() = " + a1.points.size());
//		buildPointPairList(a1, a2);
		NearestNeighbours nnlst = a2.kdtree.getNearestNeighbours((KDNode) a1.points.get(0), 2);
		System.out.println("usedSearchSteps  = " + a2.kdtree.usedSearchSteps);
		System.out.println("searchSteps      = " + a2.kdtree.searchSteps);
		System.out.println("nnlst.size()     = " + nnlst.size());		
		System.out.println("nnlst.countAdds  = " + nnlst.countAdds);		
			
		//fou.close();
	}

}
