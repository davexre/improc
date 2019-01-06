package com.slavi.ann.test.v2.test;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.dataset.MatrixDataPointPair;
import com.slavi.ann.test.dataset.MatrixTestData;
import com.slavi.ann.test.v2.connection.FullyConnectedLayer;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Util;

public class LSA_ANN_test {

	void doIt() throws Exception {
		List<MatrixDataPointPair> trainset = MatrixTestData.generatePoints(false);
//		List<? extends DatapointPair> trainset = IrisData.readDataSet(true).subList(0, 20);
		DatapointPair pair = trainset.get(0);
		Matrix input = new Matrix();
		Matrix output = new Matrix();
		Matrix error = new Matrix();
		pair.toInputMatrix(input);
		pair.toOutputMatrix(output);

		FullyConnectedLayer l = new FullyConnectedLayer(input.getVectorSize(), output.getVectorSize(), 1);
		FullyConnectedLayer.Workspace w = l.createWorkspace();

		int paramCount = l.getNumAdjustableParams();
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(paramCount);
		Matrix params = new Matrix(paramCount, 1);

		MatrixStatistics st = new MatrixStatistics();

		for (int iteration = 0; iteration < 10; iteration++) {
			System.out.println("Iteration " + iteration);
			System.out.println("--------------");

			st.start();
			lsa.clear();
			for (int pairIndex = 0; pairIndex < trainset.size(); pairIndex++) {
				pair = trainset.get(pairIndex);
				pair.toInputMatrix(input);
				pair.toOutputMatrix(output);

				Matrix out = w.feedForward(input);
				out.mSub(output, error);
/*
				st.addValue(error);
				double e = 0;
				for (int i = 0; i < error.getVectorSize(); i++) {
					double d = error.getVectorItem(i);
					e += d * d;
				}

				params.make0();
				Matrix in = w.backPropagate(params, 0, error);
				lsa.addMeasurement(params, 1, e / 2.0, 0); */

				st.addValue(error);
				Matrix err = error.makeCopy();
				for (int i = 0; i < error.getVectorSize(); i++) {
					err.make0();
					err.setVectorItem(i, error.getVectorItem(i));
					params.make0();
					Matrix in = w.backPropagate(params, 0, error);
					lsa.addMeasurement(params, 1, error.getVectorItem(i), 0);
				}
			}
			st.stop();

			ObjectMapper om = Util.jsonMapper();
			System.out.println(om.writeValueAsString(lsa.getNm()));

			boolean lsaCalculate = lsa.calculate();
			l.extractParams(params, 0);
			System.out.println(lsaCalculate ? "Adjust ok" : "Adjust FAILED");
			params.printM("Params");

			Matrix u = lsa.getUnknown();
			u.transpose();
			u.printM("UNKNOWNS");
			params.mSum(u, params);
			u.transpose();
			l.loadParams(params, 0);

			st.getAbsMaxX().printM("MaxError");
			st.getSumSquares().printM("E*E");
			System.out.println("Sum(E*E) = " + st.getSumSquares().sumAll());
		}
	}

	public static void main(String[] args) throws Exception {
		new LSA_ANN_test().doIt();
		System.out.println("Done.");
	}
}
