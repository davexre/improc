package com.slavi.img;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import com.slavi.utils.Marker;
import com.slavi.utils.XMLHelper;

public class Test {
	private static String workDir = "./../../images/";
	private static String srcImg = workDir + "testimg.bmp";
	
	public static void testDImageMap() throws Exception {
		DImageMap img = new DImageMap(new File(srcImg));
		DImageMap blur = new DImageMap(new File(srcImg));
		DImageMap dir = new DImageMap(new File(srcImg));
		DImageMap mag = new DImageMap(new File(srcImg));
		DGaussianFilter gf = new DGaussianFilter();
		Marker.mark();
		ImageIO.write(blur.toImage(), "bmp", new File(workDir + "testblur.bmp"));
		for (int i = 0; i <= 4; i++) {
			gf.applyGaussianFilter(img, blur);
			blur.computeDirection(dir);
			blur.computeMagnitude(mag);
			ImageIO.write(blur.toImage(), "bmp", new File(workDir + "testblur" + i
					+ ".bmp"));
			ImageIO.write(dir.toImage(), "bmp", new File(workDir + "testdir" + i
					+ ".bmp"));
			ImageIO.write(mag.toImage(), "bmp", new File(workDir + "testmag" + i
					+ ".bmp"));
			System.out.println("blur min=" + blur.min() + " max=" + blur.max());
			System.out.println("dir  min=" + dir.min() + " max=" + dir.max());
			System.out.println("mag  min=" + mag.min() + " max=" + mag.max());

			DImageMap tmp = blur;
			blur = img;
			img = tmp;
		}
		Marker.release();
	}

	public static void testDGaussianFilter() throws Exception {
		DImageMap img = new DImageMap(new File(srcImg));
		DImageMap blur = new DImageMap(new File(srcImg));
		DGaussianFilter gf = new DGaussianFilter();
		Marker.mark();
		ImageIO.write(blur.toImage(), "bmp", new File(workDir + "testblur.bmp"));
		for (int i = 0; i <= 4; i++) {
			gf.applyGaussianFilter(img, blur);
			ImageIO.write(blur.toImage(), "bmp", new File(workDir + "testblur" + i
					+ ".bmp"));
			System.out.println("min=" + blur.min() + " max=" + blur.max());
			DImageMap tmp = blur;
			blur = img;
			img = tmp;
		}
		Marker.release();
	}

	public static void compareApplyFilters() throws Exception {
		DImageMap img = new DImageMap(new File(srcImg));
		DImageMap blur1 = new DImageMap(img.sizeX, img.sizeY);
		DImageMap blur2 = new DImageMap(img.sizeX, img.sizeY);
		DGaussianFilter gf = new DGaussianFilter();
		gf.applyGaussianFilter(img, blur1);
		gf.applyGaussianFilterOriginal(img, blur2);
		double maxD = 0;
		double sumD = 0;
		for (int i = 0; i < img.sizeX; i++)
			for (int j = 0; j < img.sizeY; j++) {
				double d = Math.abs(blur1.pixel[i][j] - blur2.pixel[i][j]);
				sumD += d;
				maxD = Math.max(maxD, d);  
			}
		System.out.println(sumD);
		System.out.println(maxD);
	}

	public static void compareKeyFiles(String fname1, String fname2) throws Exception {
		ScalePointList list1= ScalePointList.fromXML(XMLHelper.readXML(new File(fname1)));
		ScalePointList list2= ScalePointList.fromXML(XMLHelper.readXML(new File(fname2)));
		ArrayList list1Points =list1.kdtree.toList(); 
		ArrayList list2Points =list2.kdtree.toList(); 
		int matchedCount1 = 0;
		for (int i = list1Points.size() - 1; i >= 0; i--) {
			ScalePoint sp1 = (ScalePoint)list1Points.get(i);
			boolean matchingFound = false;
			for (int j = list2Points.size() - 1; j >= 0; j--) {
				ScalePoint sp2 = (ScalePoint)list2Points.get(j);
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
		for (int j = list2Points.size() - 1; j >= 0; j--) {
			ScalePoint sp2 = (ScalePoint)list2Points.get(j);
			boolean matchingFound = false;
			for (int i = list1Points.size() - 1; i >= 0; i--) {
				ScalePoint sp1 = (ScalePoint)list1Points.get(i);
				if (sp1.equals(sp2)) {
					matchingFound = true;
					matchedCount2++;
					break;
				}
			}
			if (!matchingFound)
				System.out.println("Point No " + j + " from 2-nd list has no match in 1-st list /X=" + sp2.doubleX + ",Y=" + sp2.doubleY);
		}
		
		System.out.println("Matched 1-st list against 2-nd list: " + matchedCount1 + "/" + list1Points.size());
		System.out.println("Matched 2-nd list against 1-st list: " + matchedCount2 + "/" + list2Points.size());
		System.out.println("Finished!");
	}	
	
	
	public static class Data {
		public int x;
		public int y;
		public int level;
		public int adjS;
		public int bins[] = new int[1000];
		public static final int DIGITS = 100000;
		
		public static Data fromString(String str) {
			Data r = new Data();
			StringTokenizer st = new StringTokenizer(str);
			for (int i = 0; i < r.bins.length; i++) r.bins[i] = 0;
			r.x = (int) (Double.parseDouble(st.nextToken()) * DIGITS);
			r.y = (int) (Double.parseDouble(st.nextToken()) * DIGITS);
			r.level = Integer.parseInt(st.nextToken());
			r.adjS = (int) (Double.parseDouble(st.nextToken()) * DIGITS);
			int i = 0;
			while (st.hasMoreTokens())
				r.bins[i++] = (int) (Double.parseDouble(st.nextToken()) * DIGITS);
			return r;
		}
		
		public boolean equals(Object ob) {
			Data d = (Data)ob;
			for (int i = 0; i < bins.length; i++)
				if (bins[i] != d.bins[i])
					return false;
			return (x==d.x) && (y==d.y) && (level==d.level) && (adjS==d.adjS);			
		}
	}

	public static void compareFiles(String fname1, String fname2) throws Exception {
		ArrayList list1 = new ArrayList();
		ArrayList list2= new ArrayList();
		
		BufferedReader fin1 = new BufferedReader(new FileReader(fname1));
		BufferedReader fin2 = new BufferedReader(new FileReader(fname2));

		fin1.readLine();
		while (fin1.ready()) {
			list1.add(Data.fromString(fin1.readLine()));
		}
		fin2.readLine();
		while (fin2.ready()) {
			list2.add(Data.fromString(fin2.readLine()));
		}
				
		int matchedCount1 = 0;
		for (int i = list1.size() - 1; i >= 0; i--) {
			Data sp1 = (Data)list1.get(i);
			boolean matchingFound = false;
			for (int j = list2.size() - 1; j >= 0; j--) {
				Data sp2 = (Data)list2.get(j);
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
		for (int j = list2.size() - 1; j >= 0; j--) {
			Data sp2 = (Data)list2.get(j);
			boolean matchingFound = false;
			for (int i = list1.size() - 1; i >= 0; i--) {
				Data sp1 = (Data)list1.get(i);
				if (sp1.equals(sp2)) {
					matchingFound = true;
					matchedCount2++;
					break;
				}
			}
			if (!matchingFound)
				System.out.println("Point No " + j + " from 2-nd list has no match in 1-st list");
		}
		
		System.out.println("Matched 1-st list against 2-nd list: " + matchedCount1 + "/" + list1.size());
		System.out.println("Matched 2-nd list against 1-st list: " + matchedCount2 + "/" + list2.size());
		System.out.println("Finished!");
	}	
	
	public static void main(String[] args) {
		//Test t = new Test();
		try {
//			String fn1 = workDir + "1-A.jpg";
//			String fn2 = workDir + "1-A.b.jpg";
//			Test.compareBImages(fn1, fn2);
			Test.compareFiles(workDir + "debug.my", workDir + "debug.s.my");
//			Test.compareKeyFiles(workDir + "testimg.APxml", workDir + "testimg.xml");
//			Test.compareKeyFiles(workDir + "my_keys.xml", workDir + "my_keys_working.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
