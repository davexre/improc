package com.slavi.ann.test.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import com.slavi.ann.test.DatapointPair;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.io.CommentAwareLineNumberReader;
import com.slavi.util.io.LineReader;

public class ConvolutionTestData {
	public static Matrix loadMatrix(int sizeX, int sizeY, LineReader fin) throws IOException {
		Matrix r = new Matrix(sizeX, sizeY);
		for (int j = 0; j < sizeY; j++) {
			StringTokenizer st = new StringTokenizer(fin.readLine());
			for (int i = 0; i < sizeX; i++)
				r.setItem(i, j, st.hasMoreTokens() ? Double.parseDouble(st.nextToken()) : 0.0);
		}
		return r;
	}

	public static class ConvolutionTestDataPoint implements DatapointPair {
		String name;
		Matrix input;
		Matrix output;

		@Override
		public void toInputMatrix(Matrix dest) {
			input.copyTo(dest);
		}

		@Override
		public void toOutputMatrix(Matrix dest) {
			output.copyTo(dest);
		}

		@Override
		public String getName() {
			return name;
		}
	}

	public static List<ConvolutionTestDataPoint> readDataSet(InputStream is) throws IOException {
		List<ConvolutionTestDataPoint> r = new ArrayList<>();
		try (LineReader lr = new CommentAwareLineNumberReader(new InputStreamReader(is))) {
			int inSizeX = Integer.parseInt(lr.readLine());
			int inSizeY = Integer.parseInt(lr.readLine());

			while (true) {
				String name = lr.readLine();
				if (StringUtils.trimToNull(name) == null)
					break;
				ConvolutionTestDataPoint p = new ConvolutionTestDataPoint();
				p.name = name;
				p.input = loadMatrix(inSizeX, inSizeY, lr);
				p.output = loadMatrix(inSizeX, inSizeY, lr);
				r.add(p);
			}
		}
		return r;
	}
}
