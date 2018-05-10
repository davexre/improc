package com.slavi.ann.test.dataset;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.Utils;
import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;

public class MnistData {
	static final String dataUrl = "http://yann.lecun.com/exdb/mnist/";
	static final String dataTargetDir = "data/mnist";

	static final String trainingFiles = "train-images-idx3-ubyte.gz";
	static final String trainingFileLabels = "train-labels-idx1-ubyte.gz";
	static final String testFiles = "t10k-images-idx3-ubyte.gz";
	static final String testFileLabels = "t10k-labels-idx1-ubyte.gz";
	static final String dataFiles[] = { trainingFiles, trainingFileLabels, testFiles, testFileLabels };

	static final double valueLow = 0.05;
	static final double valueHigh = 0.95;
	
	public static class MnistPattern implements DatapointPair {
		public int patternNumber;
		public byte label;
		public byte image[];

		public static final int columns = 28;
		public static final int rows = 28;
		public static final int size = columns * rows; // 784
		
		public BufferedImage toBufferedImage() {
			BufferedImage img = new BufferedImage(columns, rows, BufferedImage.TYPE_USHORT_GRAY);
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < columns; c++) {
					int col = ((int) image[r * columns + c]) & 0xff;
					col = col << 16 | col << 8 | col;
					img.setRGB(c, r, col);
				}
			}
			return img;
		}
		
		public void toInputMatrix(Matrix dest) {
			dest.resize(28, 28);
			for (int i = dest.getVectorSize() - 1; i >= 0; i--)
				dest.setVectorItem(i, MathUtil.mapValue(((int) image[i]) & 255, 0, 255, valueLow, valueHigh));
		}
		
		public void toOutputMatrix(Matrix dest) {
			dest.resize(10, 1);
			for (int i = 0; i < 10; i++)
				dest.setVectorItem(i, label == i ? valueHigh : valueLow);
		}
		
		public String toString() {
			return "Label: " + label + ", pattern number: " + patternNumber;
		}
	}

	public static List<MnistPattern> readMnistSet(String labelsFileName, String imagesFileName) throws Exception {
		Utils.downloadDataFiles(dataTargetDir, dataUrl, dataFiles);

		File dir = new File(dataTargetDir);
		File labelsFile = new File(dir, labelsFileName);
		File imagesFile = new File(dir, imagesFileName);
		try (
			DataInputStream labelsIs = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(labelsFile))));
			DataInputStream imagesIs = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(imagesFile))));
		) {
			int magicNumber = labelsIs.readInt();
			if (magicNumber != 2049)
				throw new Exception("Invalid labels file format");
			magicNumber = imagesIs.readInt();
			if (magicNumber != 2051)
				throw new Exception("Invalid images file format");

			int numberOfLabels = labelsIs.readInt();
			int numberOfImages = imagesIs.readInt();
			if (numberOfLabels != numberOfImages)
				throw new Exception("Number of items in files not matched.");

			int numberOfRows = imagesIs.readInt();
			int numberOfCols = imagesIs.readInt();
			if (numberOfRows != 28 || numberOfCols != 28)
				throw new Exception("Invalid images file format.");

			List<MnistPattern> result = new ArrayList<>();
			for (int i = 0; i < numberOfLabels; i++) {
				MnistPattern pat = new MnistPattern();
				pat.patternNumber = i;
				pat.label = labelsIs.readByte();
				if (pat.label < 0 || pat.label > 9)
					System.out.println(pat.label);
				pat.image = new byte[numberOfRows * numberOfCols];
				for (int index = 0; index < pat.image.length; index++)
					pat.image[index] = imagesIs.readByte();
				result.add(pat);
			}
			return result;
		}
	}

	public static List<MnistPattern> readMnistSet(boolean useTrainDataSet) throws Exception {
		if (useTrainDataSet)
			return readMnistSet(trainingFileLabels, trainingFiles);
		else
			return readMnistSet(testFileLabels, testFiles);
	}

	public static void main(String[] args) throws Exception {
		Marker.mark("Read");
		List<MnistPattern> pats = readMnistSet(false);
		Marker.release();

		// ImageIO.write(pats.get(pats.size() - 1).toBufferedImage(), "png", new File(dataTargetDir, "test.png"));
		System.out.println(pats.size());
		System.out.println("Done.");
	}
}
