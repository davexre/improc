package com.slavi.img;
/*
 * Run using the -Xmx (maximum heap) like (300 Mb)
 * java -Xmx300000000
 */
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.jdom.Element;

import com.slavi.matrix.Matrix;
import com.slavi.statistics.AffineTransformLearner;
import com.slavi.statistics.BaseTransformLearner;
import com.slavi.statistics.StatisticsLT;
import com.slavi.tree.NearestNeighbours;
import com.slavi.utils.Marker;
import com.slavi.utils.XMLHelper;

public class AutoPano {

	// fitThresh: Threshhold value for further matching of a single match. If
	// the position of the transformed pixel is more than fitThresh pixels
	// away, its considered invalid.
	public static final int fitThreshold = 4;
	
	/**
	 * The maximum radius to consider around a keypoint that is refined. That
	 * is, at most a patch of a maximum size of twice this value in both
	 * horizontal and vertical direction is extracted.
	 */
	public static final int RefinementRadiusMaximum = 96;
	
	public ScalePointPairList pointPairs;
	
	public void buildPointPairList(KeyPointList a, KeyPointList b)  {
		int denied = 0;
		ArrayList aPoints = a.kdtree.toList();
		ArrayList bPoints = b.kdtree.toList();
		if (aPoints.size() > bPoints.size()) {
			KeyPointList tmp = a;
			a = b;
			b = tmp;
			ArrayList tmpLst = aPoints;
			aPoints = bPoints;
			bPoints = tmpLst;
		}		
		int searchSteps = (int) (Math.max(130.0, (Math.log(aPoints.size()) / Math.log (1000.0)) * 130.0));
		
		for (int p = aPoints.size() - 1; p >= 0; p--) {
			KeyPoint ap = (KeyPoint) aPoints.get(p);
			NearestNeighbours nnlst = b.kdtree.getNearestNeighboursBBF(ap, 2, searchSteps);
			if (nnlst.size() < 2)
				continue;
			if (nnlst.getDistanceToTarget(0) > nnlst.getDistanceToTarget(1) * 0.6) {
				denied++;
				continue;
			}
			KeyPoint bp = (KeyPoint)nnlst.getItem(0);
			pointPairs.addPair(ap, (KeyPoint)nnlst.getItem(0), nnlst.getDistanceToTarget(0), nnlst.getDistanceToTarget(1));
			fou.println(
				ap.doubleX + "\t" + ap.doubleY + "\t" + ap.kpScale + "\t" + 
				bp.doubleX + "\t" + bp.doubleY + "\t" + bp.kpScale + "\t" +
				nnlst.getDistanceToTarget(0) + "\t" + nnlst.getDistanceToTarget(1));
		}
		System.out.println("DENIED=" + denied);
		System.out.println("SPL1.SIZE=" + aPoints.size());
		System.out.println("SPL2.SIZE=" + bPoints.size());
	}

	public void printWeightsGreaterThanOne() {
		for (int i = 0; i < pointPairs.items.size(); i++) {
			KeyPointPair pp = (KeyPointPair) pointPairs.items.get(i);
			if (pp.getWeight() > 1.0) {
				System.out.println("Item at index " + i + " has id " + pp.id + " and weight " + pp.getWeight());
			}
		}
	}

	public void resetWeights() {
		for (int i = 0; i < pointPairs.items.size(); i++) {
			KeyPointPair pp = (KeyPointPair) pointPairs.items.get(i);
			pp.setWeight(1.0);
		}
	}
	
	int maxIterations = 20;
	int minGoodPoints = 30;
	BaseTransformLearner atl;
	BaseTransformLearner atl2;
	
	public void processPointsPairList() {
		printWeightsGreaterThanOne();
		resetWeights();
//		pointPairs.sortByOverallFitness();
//		pointPairs.leaveGoodTopElements(pointPairs.items.size() / 2);

		atl = new AffineTransformLearner(2, 2, pointPairs.items);
//		atl2 = new Helmert2DTransformLearner(pointPairs.items);
//
//		System.out.println("========= HELMERT2D ================");
//		System.out.println("ADJUST = " + atl2.calculateOne());
//		System.out.println("Good count = " + pointPairs.countGoodItems());
//		pointPairs.sortByDelta();
//		pointPairs.leaveGoodTopElements(pointPairs.items.size() / 2);
//				
//		System.out.println("ADJUST = " + atl2.calculateOne());
//		System.out.println("Good count = " + pointPairs.countGoodItems());
//		System.out.println("ADJUST = " + atl2.calculateOne());
//		System.out.println("Good count = " + pointPairs.countGoodItems());
//		System.out.println("ADJUST = " + atl2.calculateOne());
//		System.out.println("Good count = " + pointPairs.countGoodItems());
//		atl2.recomputeWeights();

		System.out.println("========= AFFINE ================");
		System.out.println("ADJUST = " + atl.calculateOne());
		System.out.println("Good count = " + pointPairs.countGoodItems());
		System.out.println("ADJUST = " + atl.calculateOne());
		System.out.println("Good count = " + pointPairs.countGoodItems());
		pointPairs.sortByDelta();
		for (int i = 0; i < pointPairs.items.size(); i++) {
			KeyPointPair pp = (KeyPointPair) pointPairs.items.get(i);
			pp.setBad(pp.discrepancy > 2);				
		}
		System.out.println("Good count = " + pointPairs.countGoodItems());
		
//		atl.recomputeWeights();
		System.out.println("ADJUST = " + atl.calculateOne());
		System.out.println("Good count = " + pointPairs.countGoodItems());
		System.out.println("ADJUST = " + atl.calculateOne());
		System.out.println("Good count = " + pointPairs.countGoodItems());
//		pointPairs.leaveGoodTopElements(minGoodPoints);

		
//		resetWeights();
//		pointPairs.sortByDelta();
//		pointPairs.sortByWeight();
//		pointPairs.sortByOverallFitness();
//		pointPairs.sortByOrientationDelta();
//		atl.recomputeWeights();
//		pointPairs.leaveGoodTopElements(minGoodPoints);
//		printWeightsGreaterThanOne();
	}

	public void processPointsPairList2() {
		atl = new AffineTransformLearner(2, 2, pointPairs.items);
		resetWeights();
		
		System.out.println("ADJUST = " + atl.calculateOne());
		System.out.println("Good count = " + pointPairs.countGoodItems());
		pointPairs.sortByDelta();
		pointPairs.sortByWeight();
		for (int iteration = 1; iteration <= maxIterations; iteration++) {
			int indexThreshold = (int)(pointPairs.items.size() - (double)(pointPairs.items.size() - minGoodPoints) * iteration / maxIterations);
			for (int i = pointPairs.items.size() - 1; i >= 0; i--) {
				KeyPointPair pp = (KeyPointPair) pointPairs.items.get(i);
				pp.setBad(i >= indexThreshold);
			}
			for (int i = 0; i < minGoodPoints; i++) {
				KeyPointPair pp = (KeyPointPair) pointPairs.items.get(i);
				System.out.println(pp.discrepancy + "\t" + pp.id); 
			}
			System.out.println("ADJUST = " + atl.calculateOne());
			System.out.println("Good count = " + pointPairs.countGoodItems());
			pointPairs.sortByDelta();
			pointPairs.sortByWeight();
		}

		for (int i = 0; i < minGoodPoints; i++) {
			KeyPointPair pp = (KeyPointPair) pointPairs.items.get(i);
			System.out.println(pp.discrepancy + "\t" + pp.id); 
		}
		
		ArrayList pointPairs2 = new ArrayList();
		for (int i = pointPairs.items.size() - 1; i >= 0; i--) {
			KeyPointPair pp = (KeyPointPair) (pointPairs.items.get(i));
			if (!pp.isBad())
				pointPairs2.add(pp);
		}
		atl = new AffineTransformLearner(2, 2, pointPairs2);
		System.out.println("ADJUST = " + atl.calculateOne());
		pointPairs.sortByDelta();
		pointPairs.sortByWeight();
		
		for (int i = 0; i < minGoodPoints; i++) {
			KeyPointPair pp = (KeyPointPair) pointPairs.items.get(i);
			System.out.println(pp.discrepancy + "\t" + pp.id); 
		}
	}
	
	public void writeToPtoFile(@SuppressWarnings("hiding")
	PrintStream fou) {
		fou.println("# Hugin project file");
		fou.println("# automatically generated by autopano-sift, available at");
		fou.println("p f2 w3000 h1500 v360  n\"JPEG q90\"");
		fou.println("m g1 i0\n");
		//...
		//pto.WriteLine ("i w{0} h{1} f0 a={2} b={2} c={2} d0 e0 p{3} r{4} v={2} y{5}  u10 n\"{6}\"",
		//   fou.println("i w{0} h{1} f0 a0 b-0.01 c0 d0 e0 p{2} r{3} v180 y{4}  u10 n\"{5}\"");
		//kx.XDim, kx.YDim, refIdx, pitch, rotation, yaw, imageFile
		fou.println("i w" + pointPairs.targetImageSizeX + " h" + pointPairs.targetImageSizeY + " f0 a0 b0 c0 d0 e0 g0 p0 r0 t0 v38 y0 u10 n\"" + pointPairs.targetImageFileName + "\"");
		fou.println("i w" + pointPairs.sourceImageSizeX + " h" + pointPairs.sourceImageSizeY + " f0 a0 b0 c0 d0 e0 g0 p0 r0 t0 v38 y0 u10 n\"" + pointPairs.sourceImageFileName + "\"");
		
		fou.println();
		fou.println("v p1 r1 y1");
		fou.println();

		int pointCounter = 0;		
		System.out.println("=== Output data discrepancies ===");
		for (int i = 0; i < pointPairs.items.size(); i++) {
			KeyPointPair pp = (KeyPointPair) pointPairs.items.get(i);
			if (!pp.isBad()) {
				pointCounter++;
				//fou.println("c n{0} N{1} x{2} y{3} X{4} Y{5} t0");
				//imageNameTab[ms.File1], imageNameTab[ms.File2],m.Kp1.X, m.Kp1.Y, m.Kp2.X, m.Kp2.Y
				fou.println("c n0 N1" +
					" x" + (int) (pp.source.getItem(0, 0)) + 
					" y" + (int) (pp.source.getItem(1, 0)) +
					" X" + (int) (pp.target.getItem(0, 0)) +
					" Y" + (int) (pp.target.getItem(1, 0)) + " t0");
				System.out.println(Integer.toString(pp.id) + "\t" + Double.toString(pp.discrepancy));
//				if (pointCounter > minGoodPoints)
//					break;
			}
		}
		fou.println();
		fou.println("# match list automatically generated");
	}
	
	@SuppressWarnings("cast")
	public void computePtoFile(@SuppressWarnings("hiding")
	PrintStream fou) {
		fou.println("# Hugin project file");
		fou.println("# automatically generated by autopano-sift, available at");
		fou.println("p f2 w3000 h1500 v360  n\"JPEG q90\"");
		fou.println("m g1 i0\n");
		fou.println("i w" + pointPairs.targetImageSizeX + " h" + pointPairs.targetImageSizeY + " f0 a0 b0 c0 d0 e0 g0 p0 r0 t0 v38 y0 u10 n\"" + pointPairs.targetImageFileName + "\"");
		fou.println("i w" + pointPairs.sourceImageSizeX + " h" + pointPairs.sourceImageSizeY + " f0 a0 b0 c0 d0 e0 g0 p0 r0 t0 v38 y0 u10 n\"" + pointPairs.sourceImageFileName + "\"");
		fou.println();
		fou.println("v p1 r1 y1");
		fou.println();

		double minX = 0;
		double minY = 0;
		double maxX = 0;
		double maxY = 0;
		boolean isFirst = true;
		
		for (int i = 0; i < pointPairs.items.size(); i++) {
			KeyPointPair pp = (KeyPointPair) pointPairs.items.get(i);
			if (!pp.isBad()) {
				if (isFirst) {
					minX = maxX = pp.sourceSP.doubleX;
					minY = maxY = pp.sourceSP.doubleY;
					isFirst = false;
				} else {
					minX = Math.min(minX, pp.sourceSP.doubleX);
					maxX = Math.max(maxX, pp.sourceSP.doubleX);
					minY = Math.min(minY, pp.sourceSP.doubleY);
					maxY = Math.max(maxY, pp.sourceSP.doubleY);
				}
			}
		}
		Matrix source = new Matrix(2, 1);
		Matrix target = new Matrix(2, 1);
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++) {
				source.setItem(0, 0, minX + (double)i * (maxX - minX) / 10.0);
				source.setItem(1, 0, minY + (double)j * (maxY - minY) / 10.0);
				atl.transformer.transform(source, target);
				fou.println("c n0 N1" +
						" x" + (int) (source.getItem(0, 0)) + 
						" y" + (int) (source.getItem(1, 0)) +
						" X" + (int) (target.getItem(0, 0)) +
						" Y" + (int) (target.getItem(1, 0)) + " t0");
			}
		fou.println();
		fou.println("# match list automatically generated");
	}
	
	public void analyzePointsPairList() throws Exception {
		StatisticsLT stat = new StatisticsLT();
		stat.start();
		for (int i = 0; i < pointPairs.items.size(); i++) {
			KeyPointPair pp = (KeyPointPair) pointPairs.items.get(i);
			if (pp.getWeight() <= 1)
				continue;
			double value = pp.sourceSP.degree - pp.targetSP.degree;
			stat.addValue(value);
		}
		stat.stop();
		System.out.println(stat.toString());
		
		stat.start();
		for (int i = 0; i < pointPairs.items.size(); i++) {
			KeyPointPair pp = (KeyPointPair) pointPairs.items.get(i);
			if (pp.getWeight() > 1)
				continue;
			double value = pp.sourceSP.degree - pp.targetSP.degree;
			stat.addValue(value);
		}
		stat.stop();
		System.out.println(stat.toString());
	}
	
	public static PrintWriter fou;
	
	public static void main(String[] args) throws Exception {
		//ArrayList<KDNode>lst = new ArrayList<KDNode>();
		//lst.iterator();
		
		//AbsoluteToRelativePathMaker rootImagesDir = new AbsoluteToRelativePathMaker(rootImagesDirStr);
		//AbsoluteToRelativePathMaker rootSPfileDir = new AbsoluteToRelativePathMaker(rootSPfileDirStr);

		fou = new PrintWriter("../images/debug.my");
		AutoPano autoPano = new AutoPano();
		try {
			if (false) {
				String file1 = "../images/HPIM0336.xml";
				String file2 = "../images/HPIM0337.xml";
				//String file2 = "../images/testimg.xml";
	
				KeyPointList spl1 = KeyPointList.fromXML(XMLHelper.readXML(new File(file1)), null);
				KeyPointList spl2 = KeyPointList.fromXML(XMLHelper.readXML(new File(file2)), null);
				
				autoPano.pointPairs = new ScalePointPairList(spl1, spl2);
				
				System.out.println("buildPointPairList");
				Marker.mark();
				autoPano.buildPointPairList(spl1, spl2);
				Marker.release();
				
				Element e = new Element("ScalePointPairs");
				autoPano.pointPairs.toXML(e);
				XMLHelper.writeXML(new File("../images/ppairs.xml"), e, "matrix.xsl");
			} else {
				Element e = XMLHelper.readXML(new File("../images/ppairs.xml"));
				autoPano.pointPairs = ScalePointPairList.fromXML(e);
				autoPano.atl = null;
				System.out.println("Total number of items = " + autoPano.pointPairs.items.size());

				Marker.mark();
				autoPano.processPointsPairList();
				//autoPano.analyzePointsPairList();
				Marker.release();
				autoPano.pointPairs.sortByDelta();

				Matrix delta = autoPano.atl.computeTransformedTargetDelta(false);
				System.out.println("==== max discrepancy ====");
				System.out.println(delta.toString());
				delta = autoPano.atl.computeTransformedTargetDelta(true);
				System.out.println("==== max discrepancy 2 ====");
				System.out.println(delta.toString());

				autoPano.writeToPtoFile(new PrintStream("../images/ppairs.pto"));
				//autoPano.computePtoFile(new PrintStream("../images/ppairs2.pto"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fou.close();
	}
}
