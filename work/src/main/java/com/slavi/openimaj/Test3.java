package com.slavi.openimaj;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.contour.Contour;
import org.openimaj.image.contour.SuzukiContourProcessor;
import org.openimaj.image.feature.dense.binarypattern.BasicLocalBinaryPattern;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;

public class Test3 {

	void doIt() throws Exception {
		File f = new File("/home/spetrov/.S/temp/1/20160204 Софийски университет 8.jpg");
		MBFImage image = ImageUtilities.readMBF(f);
		FImage fimage = image.flatten();
/*		DenseSIFT d = new DenseSIFT();
		d.analyseImage(fimage);
		StringWriter out = new StringWriter();
		PrintWriter sw = new PrintWriter(out);
		d.getFloatKeypoints().writeASCII(sw);
		System.out.println(out.toString());*/
		
/*		BasicLocalBinaryPattern p = new BasicLocalBinaryPattern();
		p.analyseImage(fimage);
		p.getPattern();
*/		//new FImage();
		
		
		//WatershedProcessorAlgorithm a = new WatershedProcessorAlgorithm(fimage, new IntValuePixel(0, 0), new Class[0]);
		//a.startPour();
		DisplayUtilities.display("asd", 1, image, fimage);
	}

	public static void main(String[] args) throws Exception {
		new Test3().doIt();
		System.out.println("Done.");
	}
}
