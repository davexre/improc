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

import com.slavi.math.MathUtil;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;
/*

To check this: https://github.com/deepmind/sonnet
more info: http://yann.lecun.com/exdb/publis/index.html#lecun-98

*/
public class MyMnistData {

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

	int insize = 28*28;
	void patToInput(MnistPattern pat, Matrix dest) {
		dest.resize(insize, 1);
		for (int i = 0; i < insize; i++)
			dest.setVectorItem(i, MathUtil.mapValue(pat.image[i], 0, 255, 0, 1));
	}

	void patToOutput(MnistPattern pat, Matrix dest) {
		dest.resize(10, 1);
		for (int i = 0; i < 10; i++)
			dest.setVectorItem(i, pat.label == i ? 1 : 0);
	}

	void doIt() throws Exception {
		//downloadMnistFiles();

		Marker.mark("Read");
		List<MnistPattern> pats = readMnistSet(testFileLabels, testFiles);
		//List<MnistPattern> pats = readMnistSet(trainingFileLabels, trainingFiles);
		Marker.release();

		// ImageIO.write(pats.get(pats.size() - 1).toBufferedImage(), "png", new File(mnistDir, "test.png"));

		MyNet nnet = new MyNet(MyLayer.class,
				insize,
				700, 600, 500, 400, 300, 200, 100,
				50, 10);
		nnet.eraseMemory();

		int maxPattern = 100; //pats.size();
		int maxPatternTrain = maxPattern; // / 2;

		Marker.mark("Total");
		Marker.mark("Train");
		Matrix input = new Matrix(nnet.getSizeInput(), 1);
		Matrix op = new Matrix(nnet.getSizeOutput(), 1);
		for (int epoch = 0; epoch < 1; epoch++)
			for (int index = 0;
					index < maxPatternTrain; //pats.size()
					index++) {
				MnistPattern pat = pats.get(index);
				patToInput(pat, input);
				patToOutput(pat, op);
				Matrix t = nnet.feedForward(input);
				op.mSub(t, op);
				nnet.backPropagate(op);
			}
		Marker.releaseAndMark("Recall");

		nnet.layers.get(nnet.layers.size() - 1).tmpW.printM("last tmpW");
		nnet.layers.get(nnet.layers.size() - 1).sumDW.printM("last sumDW");
		nnet.applyTraining();

		Matrix max = new Matrix(nnet.getSizeOutput(), 1);
		max.make0();

		Statistics st = new Statistics();
		st.start();
		Statistics st2 = new Statistics();
		st2.start();
		for (int index = 0;
				index < maxPattern; //pats.size()
				index++) {
			MnistPattern pat = pats.get(index);
			patToInput(pat, input);
			patToOutput(pat, op);
			Matrix t = nnet.feedForward(input);
			op.mSub(t, op);
			op.termAbs(op);

			for (int i = 0; i < op.getVectorSize(); i++) {
				double e = op.getVectorItem(i);
				if (e >= 0.5)
					st.addValue(e);
				else
					st2.addValue(e);
			}
			//st.addValue(op.max());
			//st2.addValue(op.min());
			max.mMax(op, max);
		}
		st.stop();
		st2.stop();

		max.printM("MAX");
		System.out.println(st.toString());
		System.out.println("MIN");
		System.out.println(st2.toString());
		Marker.release();
		nnet.layers.get(nnet.layers.size() - 1).weight.printM("last W");

	}

	public static void main(String[] args) throws Exception {
		new MyMnistData().doIt();
//		System.out.println("Done.");
	}
}
