package com.slavi.ann.test.dataset;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.Utils;
import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;

/**
 * https://www.cs.toronto.edu/~kriz/cifar.html
 * @see http://horatio.cs.nyu.edu/mit/tiny/data/index.html
 */
public class Cifar10 {
	static final String dataUrl = "https://www.cs.toronto.edu/~kriz/";
	static final String dataFiles[] = { "cifar-10-binary.tar.gz" };
	static final String dataTargetDir = "data/cifar";

	public static class Cifar10Pattern implements DatapointPair {
		public List<String> labels;
		public int batch;
		public int patternNumber;

		public int label;
		public byte pixels[];

		public static final int columns = 32;
		public static final int rows = 32;
		public static final int size = columns * rows; // 1024 * 3 bytes per pixel = 3072

		public BufferedImage toBufferedImage() {
			BufferedImage img = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_RGB);
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < columns; c++) {
					int offset = r * columns + c;
					int col =
						(((int) pixels[offset]) & 0xff) << 16 |
						(((int) pixels[offset + size]) & 0xff) << 8 |
						(((int) pixels[offset + size + size]) & 0xff);
					img.setRGB(c, r, col);
				}
			}
			return img;
		}

		public void toInputMatrix(Matrix dest) {
			dest.resize(columns, rows);
			for (int i = dest.getSizeX() - 1; i >= 0; i--)
				for (int j = dest.getSizeY() - 1; j >= 0; j--)
					dest.setItem(i, j, MathUtil.mapValue(pixels[j * columns + i] & 0xff, 0, 255, Utils.valueLow, Utils.valueHigh));
		}

		public void toOutputMatrix(Matrix dest) {
			dest.resize(10, 1);
			for (int i = dest.getVectorSize() - 1; i >= 0; i--)
				dest.setVectorItem(i, label == i ? Utils.valueHigh : Utils.valueLow);
		}

		public String toString() {
			return String.format("Label: %s, batch: %s, pattern number: %s (%s)" ,
					label, batch, patternNumber, labels.get(label));
		}

		public String getName() {
			return Integer.toString(patternNumber);
		}
	}

	public static void readSingleDataFile(InputStream is, int batch, List<Cifar10Pattern> result, List<String> labels) throws IOException {
		int label;
		int count = 0;
		while ((label = is.read()) >= 0) {
			Cifar10Pattern p = new Cifar10Pattern();
			p.labels = labels;
			p.label = label;
			p.batch = batch;
			p.patternNumber = count++;
			p.pixels = new byte[3072];
			int off = 0;
			while ((off += is.read(p.pixels, off, p.pixels.length - off)) < p.pixels.length)
				;
			result.add(p);
		}
	}

	public static List<Cifar10Pattern> readDataSet() throws Exception {
		Utils.downloadDataFiles(dataTargetDir, dataUrl, dataFiles);
		try (
			InputStream fin = new GZIPInputStream(new FileInputStream(new File(dataTargetDir, dataFiles[0])));
			TarArchiveInputStream tarInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", fin);
		) {
			ArrayList<Cifar10Pattern> result = new ArrayList<>();
			ArrayList<String> labels = new ArrayList<>();
			TarArchiveEntry entry = null;
			Pattern pattern = Pattern.compile(".*/data_batch_(\\d+).bin");
			while ((entry = (TarArchiveEntry)tarInputStream.getNextEntry()) != null) {
				String fname = entry.getName();
				//System.out.println(entry.getName());
				if (fname.endsWith("batches.meta.txt")) {
					labels.addAll(IOUtils.readLines(tarInputStream));
				} else if (fname.endsWith("test_batch.bin")) {
					readSingleDataFile(tarInputStream, 0, result, labels);
				} else {
					Matcher matcher = pattern.matcher(fname);
					if (matcher.matches()) {
						int batch = Integer.parseInt(matcher.group(1));
						readSingleDataFile(tarInputStream, batch, result, labels);
					}
				}
			}
			return result;
		}
	}

	public static void main(String[] args) throws Exception {
		Marker.mark("Read");
		List<Cifar10Pattern> pats = readDataSet();
		Marker.release();

		Cifar10Pattern pat = pats.get(pats.size() - 2);
		System.out.println(pat);
		ImageIO.write(pat.toBufferedImage(), "png", new File(dataTargetDir, "test.png"));
		System.out.println(pats.size());
		System.out.println("Done.");
	}
}
