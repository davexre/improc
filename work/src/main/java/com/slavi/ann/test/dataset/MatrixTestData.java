package com.slavi.ann.test.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import com.slavi.ann.test.v2.Layer;
import com.slavi.ann.test.v2.Utils;
import com.slavi.ann.test.v2.connection.ConvolutionLayer;
import com.slavi.ann.test.v2.connection.FullyConnectedLayer;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.io.CommentAwareLineNumberReader;
import com.slavi.util.io.LineReader;

public class MatrixTestData {
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
		List<MatrixDataPointPair> r = new ArrayList<>();
		FullyConnectedLayer l = new FullyConnectedLayer(weight.getSizeX(), weight.getSizeY(), 1);
		FullyConnectedLayer.Workspace w = l.createWorkspace();
		weight.copyTo(l.weight);
		for (int i = 0; i < numberOfDatapoints; i++) {
			MatrixDataPointPair p = new MatrixDataPointPair();
			p.name = Integer.toString(i);
			p.input = new Matrix(weight.getSizeX(), 1);
			Utils.randomMatrix(p.input);
			p.output = w.feedForward(p.input).makeCopy();
			r.add(p);
		}
		return r;
	}
}
