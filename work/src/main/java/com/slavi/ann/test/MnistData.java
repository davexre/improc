package com.slavi.ann.test;

import java.awt.image.BufferedImage;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.ann.ANN;
import com.slavi.ann.NNSimpleLayer;
import com.slavi.ann.NNet;
import com.slavi.math.MathUtil;
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

	static class MnistPattern {
		public int patternNumber;
		public byte label;
		public byte image[];

		public BufferedImage toBufferedImage() {
			int numberOfCols = 28;
			int numberOfRows = 28;

			BufferedImage img = new BufferedImage(numberOfCols, numberOfRows, BufferedImage.TYPE_USHORT_GRAY);
			for (int r = 0; r < numberOfRows; r++) {
				for (int c = 0; c < numberOfCols; c++) {
					int col = ((int) image[r * numberOfCols + c]) & 0xff;
					col = col << 16 | col << 8 | col;
					img.setRGB(c, r, col);
				}
			}
			return img;
		}
	}

	void downloadMnistFiles() throws Exception {
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

	List<MnistPattern> readMnistSet(String labelsFileName, String imagesFileName) throws Exception {
		File mnistDir = new File(this.mnistDir);
		File labelsFile = new File(mnistDir, labelsFileName);
		File imagesFile = new File(mnistDir, imagesFileName);
		try (
			DataInputStream labelsIs = new DataInputStream(new GZIPInputStream(new FileInputStream(labelsFile)));
			DataInputStream imagesIs = new DataInputStream(new GZIPInputStream(new FileInputStream(imagesFile)))
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

	void doIt() throws Exception {
		//downloadMnistFiles();
		
		Marker.mark("Read");
		//List<MnistPattern> pats = readMnistSet(testFileLabels, testFiles);
		List<MnistPattern> pats = readMnistSet(trainingFileLabels, trainingFiles);
		Marker.release();
		
		// ImageIO.write(pats.get(pats.size() - 1).toBufferedImage(), "png", new File(mnistDir, "test.png"));

		ObjectMapper mapper = Utils.jsonMapper();
		int insize = 28*28;
		NNet nnet = new NNet(NNSimpleLayer3.class,
				insize,
				10, 10);
		nnet.setLearningRate(1);
		nnet.setMomentum(1);
		nnet.eraseMemory();

		int maxPattern = pats.size();
		int maxPatternTrain = maxPattern / 2;

		Marker.mark("Total");
		Marker.mark("Train");
		double input[] = new double[insize];
		double op[] = new double[10];
		//for (int epoch = 0; epoch < 1; epoch++)
			for (int index = 0;
					index < maxPatternTrain; //pats.size()
					index++) {
				MnistPattern pat = pats.get(index);
				for (int i = 0; i < insize; i++)
					input[i] = MathUtil.mapValue(pat.image[i], 0, 255, 0, 1);
				for (int i = 0; i < 10; i++)
					op[i] = pat.label == i ? 1 : 0;
				nnet.feedForward(input);
				double[] er = nnet.getOutput();
				for (int i = er.length - 1; i >= 0; i--)
					er[i] = op[i] - er[i];
				nnet.backPropagate(er);
			}
		Marker.releaseAndMark("Recall");
		double max[] = new double[10];
		ANN.zeroArray(max);

		for (int index = 0;
				index < maxPattern; //pats.size()
				index++) {
			MnistPattern pat = pats.get(index);
			for (int i = 0; i < insize; i++)
				input[i] = MathUtil.mapValue(input[i], 0, 255, 0, 1);
			for (int i = 0; i < 10; i++)
				op[i] = pat.label == i ? 1 : 0;
			nnet.feedForward(input);
			double[] er = nnet.getOutput();
			for (int i = er.length - 1; i >= 0; i--)
				er[i] = op[i] - er[i];
			for (int i = er.length - 1; i >= 0; i--)
				max[i] = Math.max(max[i], er[i]);
		}

		System.out.print("[");
		for (int i = 0; i < max.length; i++)
			System.out.print(String.format("%8.5f ", max[i]));
		System.out.println("]");
		//System.out.println(mapper.writeValueAsString(nnet));
		Marker.release();

	}

	public static void main(String[] args) throws Exception {
		new MnistData().doIt();
//		System.out.println("Done.");
	}
}
