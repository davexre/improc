package com.slavi.ann.test.v2;

import java.util.List;

import com.slavi.ann.test.dataset.MatrixDataPointPair;
import com.slavi.ann.test.dataset.MatrixTestData;
import com.slavi.ann.test.v2.connection.FullyConnectedLayer;

public class PolynomialTest {
	int inputSizeX = 3;
	int inputSizeY = 3;
	int outputSize = 3;

	List<MatrixDataPointPair> generate() throws Exception {
		FullyConnectedLayer l = new FullyConnectedLayer(inputSizeX * inputSizeY, outputSize, 1);
		NetworkBuilder nb = new NetworkBuilder(inputSizeX, inputSizeY)
				.addLayer(l)
				.addConstScaleAndBiasLayer(5, -10)
				.addSigmoidLayer()
				;
		List<MatrixDataPointPair> trainset = MatrixTestData.generateDataSet(nb.build(), inputSizeX, inputSizeY, 500);
		MatrixTestData.checkDataSet(trainset);
		return trainset;
	}

	void doIt() throws Exception {
		List<MatrixDataPointPair> trainset = generate();

		for (MatrixDataPointPair pair : trainset) {

		}

	}

	public static void main(String[] args) throws Exception {
		new PolynomialTest().doIt();
		System.out.println("Done.");
	}
}
