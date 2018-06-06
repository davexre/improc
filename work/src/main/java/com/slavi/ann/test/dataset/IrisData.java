package com.slavi.ann.test.dataset;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.Utils;
import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;

public class IrisData {
	static final String dataUrl = "http://download.tensorflow.org/data/";
	static final String dataFiles[] = { "iris_training.csv", "iris_test.csv" };
	static final String dataTargetDir = "data/iris";

	static final double valueLow = 0.05;
	static final double valueHigh = 0.95;

	public static class IrisPattern implements DatapointPair {
		public int patternNumber;
		public byte label;
		public float features[];

		public void toInputMatrix(Matrix dest) {
			dest.resize(features.length, 1);
			for (int i = dest.getVectorSize() - 1; i >= 0; i--)
				dest.setVectorItem(i, MathUtil.mapValue(features[i], 0, 10, 0, 1));
		}

		public void toOutputMatrix(Matrix dest) {
			dest.resize(3, 1);
			for (int i = 0; i < 3; i++)
				dest.setVectorItem(i, label == i ? valueHigh : valueLow);
		}

		public String toString() {
			return "Label: " + label + ", pattern number: " + patternNumber;
		}

		public String getName() {
			return Integer.toString(patternNumber);
		}
	}

	public static List<IrisPattern> readDataSet(boolean useTrainDataSet) throws Exception {
		Utils.downloadDataFiles(dataTargetDir, dataUrl, dataFiles);

		try (
			Reader fin = new FileReader(new File(dataTargetDir, dataFiles[useTrainDataSet ? 0 : 1]));
			LineNumberReader r = new LineNumberReader(fin);
		) {
			try {
				String s[] = r.readLine().split(",");
				int numPatterns = Integer.parseInt(s[0]);
				int numFeatures = Integer.parseInt(s[1]);
				ArrayList<String> names = new ArrayList<>();
				for (int i = 2; i < s.length; i++) {
					names.add(StringUtils.trimToEmpty(s[i]));
				}

				ArrayList<IrisPattern> result = new ArrayList<>();
				for (int p = 0; p < numPatterns; p++) {
					s = r.readLine().split(",");
					IrisPattern pat = new IrisPattern();
					pat.patternNumber = p;
					pat.features = new float[numFeatures];
					for (int f = 0; f < numFeatures; f++) {
						pat.features[f] = Float.parseFloat(s[f]);
						pat.label = Byte.parseByte(s[numFeatures]);
					}
					result.add(pat);
				}
				return result;
			} catch (Exception e) {
				System.err.println("Error at line " + r.getLineNumber());
				e.printStackTrace();
				throw e;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Marker.mark("Read");
		List<IrisPattern> pats = readDataSet(true);
		Marker.release();

		// ImageIO.write(pats.get(pats.size() - 1).toBufferedImage(), "png", new File(dataDir, "test.png"));
		System.out.println(pats.size());
		System.out.println("Done.");
	}
}
