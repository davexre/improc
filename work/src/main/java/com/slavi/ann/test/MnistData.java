package com.slavi.ann.test;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;

public class MnistData {

	Logger log = LoggerFactory.getLogger(getClass());

	String mnistUrl = "http://yann.lecun.com/exdb/mnist/";
	String mnistDir = "data/mnist";

	String trainingFiles = "train-images-idx3-ubyte.gz";
	String trainingFileLabels = "train-labels-idx1-ubyte.gz";
	String testFiles = "t10k-images-idx3-ubyte.gz";
	String testFileLabels = "t10k-labels-idx1-ubyte.gz";
	String mnistFiles[] = { trainingFiles, trainingFileLabels, testFiles, testFileLabels };

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
				dest.setVectorItem(i, MathUtil.mapValue(((int) image[i]) & 255, 0, 255, 0.05, 0.95));
		}
		
		public void toOutputMatrix(Matrix dest) {
			dest.resize(10, 1);
			for (int i = 0; i < 10; i++)
				dest.setVectorItem(i, label == i ? 0.95 : 0.05);
		}
	}

	public void downloadMnistFiles() throws Exception {
		File mnistDir = new File(this.mnistDir);
		mnistDir.mkdirs();
		URL mnistUrl = new URL(this.mnistUrl);
		for (String f : mnistFiles) {
			File targetFile = new File(mnistDir, f);
			if (!targetFile.isFile()) {
				log.info("Downloading file {}", targetFile);
				FileUtils.copyURLToFile(new URL(mnistUrl, f), targetFile);
			}
		}
	}

	public List<MnistPattern> readMnistSet(String labelsFileName, String imagesFileName) throws Exception {
		downloadMnistFiles();

		File mnistDir = new File(this.mnistDir);
		File labelsFile = new File(mnistDir, labelsFileName);
		File imagesFile = new File(mnistDir, imagesFileName);
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
		MnistData md = new MnistData();
		if (useTrainDataSet)
			return md.readMnistSet(md.trainingFileLabels, md.trainingFiles);
		else
			return md.readMnistSet(md.testFileLabels, md.testFiles);
	}

	public static void main(String[] args) throws Exception {
		Marker.mark("Read");
		List<MnistPattern> pats = readMnistSet(false);
		Marker.release();

		// ImageIO.write(pats.get(pats.size() - 1).toBufferedImage(), "png", new File(mnistDir, "test.png"));
		System.out.println(pats.size());
		System.out.println("Done.");
	}
}
