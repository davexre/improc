package com.slavi.ann.test.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slavi.ann.test.Utils;
import com.slavi.ann.test.v2.Layer;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.activation.SigmoidLayer;
import com.slavi.ann.test.v2.connection.FullyConnectedLayer;
import com.slavi.math.MathUtil;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.io.CommentAwareLineNumberReader;
import com.slavi.util.io.LineReader;

public class MatrixTestData {
	static Logger log = LoggerFactory.getLogger(MatrixTestData.class);

	public static Matrix loadMatrix(int sizeX, int sizeY, LineReader fin) throws IOException {
		Matrix r = new Matrix(sizeX, sizeY);
		for (int j = 0; j < sizeY; j++) {
			StringTokenizer st = new StringTokenizer(fin.readLine());
			for (int i = 0; i < sizeX; i++)
				r.setItem(i, j, st.hasMoreTokens() ? Double.parseDouble(st.nextToken()) : 0.0);
		}
		return r;
	}

	public static List<MatrixDataPointPair> readDataSet(InputStream is) throws IOException {
		List<MatrixDataPointPair> r = new ArrayList<>();
		try (LineReader lr = new CommentAwareLineNumberReader(new InputStreamReader(is))) {
			int inSizeX = Integer.parseInt(lr.readLine());
			int inSizeY = Integer.parseInt(lr.readLine());

			while (true) {
				String name = lr.readLine();
				if (StringUtils.trimToNull(name) == null)
					break;
				MatrixDataPointPair p = new MatrixDataPointPair();
				p.name = name;
				p.input = loadMatrix(inSizeX, inSizeY, lr);
				p.output = loadMatrix(inSizeX, inSizeY, lr);
				r.add(p);
			}
		}
		return r;
	}

	public static List<MatrixDataPointPair> generateConvolutionDataSet(Layer l, int inputSizeX, int inputSizeY, int numberOfDatapoints) {
		List<MatrixDataPointPair> r = new ArrayList<>();
		Layer.LayerWorkspace w = l.createWorkspace();
		MatrixStatistics msi = new MatrixStatistics();
		MatrixStatistics mso = new MatrixStatistics();
		msi.start();
		mso.start();
		for (int i = 0; i < numberOfDatapoints; i++) {
			MatrixDataPointPair p = new MatrixDataPointPair();
			p.name = Integer.toString(i);
			p.input = new Matrix(inputSizeX, inputSizeY);
			Utils.randomMatrix(p.input);
			p.output = w.feedForward(p.input).makeCopy();
			msi.addValue(p.input);
			mso.addValue(p.output);
			r.add(p);
		}
		msi.stop();
		mso.stop();

		MatrixStatistics mso2 = new MatrixStatistics();
		mso2.start();
		for (MatrixDataPointPair p : r) {
			for (int i = p.output.getVectorSize() - 1; i >= 0; i--) {
				double v = p.output.getVectorItem(i);
				v = MathUtil.mapValue(v, mso.getMinX().getVectorItem(i), mso.getMaxX().getVectorItem(i), Utils.valueLow, Utils.valueHigh);
				p.output.setVectorItem(i, v);
			}
			mso2.addValue(p.output);
		}
		mso2.stop();

		log.trace("Test data statisticts for input\n" + msi.toString(Statistics.CStatAvg | Statistics.CStatStdDev | Statistics.CStatCount));
		log.trace("Test data statisticts for output\n" + mso2.toString(Statistics.CStatAvg | Statistics.CStatStdDev | Statistics.CStatMinMax));
		return r;
	}

	public static List<MatrixDataPointPair> generateDataSet(Layer l, int inputSizeX, int inputSizeY, int numberOfDatapoints) {
		List<MatrixDataPointPair> r = new ArrayList<>();
		Layer.LayerWorkspace w = l.createWorkspace();
		for (int i = 0; i < numberOfDatapoints; i++) {
			MatrixDataPointPair p = new MatrixDataPointPair();
			p.name = Integer.toString(i);
			p.input = new Matrix(inputSizeX, inputSizeY);
			Utils.randomMatrix(p.input);
			p.output = w.feedForward(p.input).makeCopy();
			r.add(p);
		}
		return r;
	}

	public static List<MatrixDataPointPair> generateFullyConnectedDataSet(Matrix weight, int numberOfDatapoints) {
		FullyConnectedLayer l = new FullyConnectedLayer(weight.getSizeX(), weight.getSizeY(), 1);
		weight.copyTo(l.weight);
		Network net = new Network(l, new SigmoidLayer());
		return generateDataSet(net, weight.getSizeX(), 1, numberOfDatapoints);
	}
}
