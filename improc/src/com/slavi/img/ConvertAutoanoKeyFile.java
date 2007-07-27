package com.slavi.img;

import java.io.File;
import java.util.List;

import org.jdom.Element;

import com.slavi.utils.Utl;
import com.slavi.utils.XMLHelper;

public class ConvertAutoanoKeyFile {

	public static void main(String[] args) throws Exception {
		String finName = "./../../images/testimg.key";
		String fouName = Utl.chageFileExtension(finName, "APxml");
		
		Element root = XMLHelper.readXML(new File(finName));
		KeyPointList spl = new KeyPointList();
		//spl.imageFileName = root.getChildText("ImageFile");
		spl.imageSizeX = Integer.parseInt(root.getChildText("XDim"));
		spl.imageSizeY = Integer.parseInt(root.getChildText("YDim"));
		
		List kpl = root.getChild("Arr").getChildren("KeypointN");
		
		for (int counter = 0; counter < kpl.size(); counter++) {
			Element key = (Element)kpl.get(counter);
			KeyPoint sp = new KeyPoint();
			sp.doubleX = Double.parseDouble(key.getChildText("X"));
			sp.doubleY = Double.parseDouble(key.getChildText("Y"));
			sp.imgX = (int)sp.doubleX;
			sp.imgY = (int)sp.doubleY;
			sp.adjS = 0;
			sp.degree = Double.parseDouble(key.getChildText("Orientation"));
			sp.level = Integer.parseInt(key.getChildText("Level"));
			sp.kpScale = Double.parseDouble(key.getChildText("Scale"));
			
//			List descr = key.getChild("Descriptor").getChildren("int");
//			for (int i = 0; i < KeyPoint.descriptorSize; i++) {
//				for (int j = 0; j < KeyPoint.descriptorSize; j++) {
//					for (int k = 0; k < KeyPoint.numDirections; k++) {
//						int index = 
//							i * KeyPoint.descriptorSize * KeyPoint.numDirections +
//							j * KeyPoint.numDirections + k;
//						//sp.setItem(i, j, k, Double.parseDouble(
//						//	((Element)descr.get(index)).getText()));
//					}
//				}
//			}
			spl.kdtree.add(sp);
		}
		root = new Element("ScalePoints");
//		spl.toXML(root);
		XMLHelper.writeXML(new File(fouName), root, "matrix.xsl");
	}
}
