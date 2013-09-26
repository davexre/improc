package com.slavi.arduino;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Util;

public class LinearLeastSquareAdjutment {

	static class Data {
		Matrix coefs;
		double L;
		
		public Data(Matrix coefs, double L) {
			this.coefs = coefs.makeCopy();
			this.L = L;
		}
	}
	
	void doIt(InputStream is) throws Exception {
		int tokensPerLine = 0;
		LeastSquaresAdjust lsa = null;
		Matrix coefs = null;
		ArrayList<Data> data = new ArrayList<Data>();
		LineNumberReader r = new LineNumberReader(new InputStreamReader(is));
		try {
			while (r.ready()) {
				String line = Util.trimNZ(r.readLine());
				if ("".equals(line) || line.startsWith(";"))
					continue;
				StringTokenizer st = new StringTokenizer(line);
				if (tokensPerLine == 0) {
					tokensPerLine = st.countTokens();
					lsa = new LeastSquaresAdjust(tokensPerLine - 1, 1);
					lsa.clear();
					coefs = new Matrix(tokensPerLine - 1, 1);
				}
				coefs.make0();
				if (tokensPerLine != st.countTokens())
					throw new Exception ("Number of tokens do not match");
				double L = Double.parseDouble(st.nextToken());
				for (int i = 0; i < coefs.getSizeX(); i++) {
					coefs.setItem(i, 0, Double.parseDouble(st.nextToken()));
				}
				data.add(new Data(coefs, L));
				lsa.addMeasurement(coefs, 1.0, L, 0);
			}
		} catch (Throwable t) {
			throw new Exception("Error while processing line " + r.getLineNumber(), t);
		} finally {
			r.close();
		}

		if (!lsa.calculate()) 
			throw new Exception("Adjust failed");
		Matrix u = lsa.getUnknown();
		u.printM("Unknowns are:");
		
		Statistics stat = new Statistics();
		stat.start();
		// Calculate discrepancies
		for (int i = 0; i < data.size(); i++) {
			Data d = data.get(i);
			double L = 0;
			for (int j = 0; j < u.getSizeY(); j++) {
				L += u.getItem(0, j) * d.coefs.getItem(j, 0);
			}
			L -= d.L;
//			stat.addValue(Math.abs(L));
			stat.addValue(L*L);
		}
		stat.stop();
		System.out.println("Discrepancy statistics:");
		System.out.println(stat);
	}
	
	public static void main(String[] args) throws Exception {
		LinearLeastSquareAdjutment t = new LinearLeastSquareAdjutment();
		t.doIt(t.getClass().getResourceAsStream("LinearLeastSquareAdjutment.txt"));
		
	}
}
