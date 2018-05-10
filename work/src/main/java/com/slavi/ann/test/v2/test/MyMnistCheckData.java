package com.slavi.ann.test.v2.test;

import java.util.List;

import com.slavi.ann.test.dataset.MnistData;
import com.slavi.ann.test.dataset.MnistData.MnistPattern;
import com.slavi.ann.test.v2.connection.ConvolutionSameSizeLayer;
import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;

public class MyMnistCheckData {

	void doIt() throws Exception {
		List<MnistPattern> pats = MnistData.readMnistSet(false);
		ConvolutionSameSizeLayer l = new ConvolutionSameSizeLayer(6, 6, 1);
		ConvolutionSameSizeLayer.LayerWorkspace w = l.createWorkspace();
		Matrix input = new Matrix();
		Matrix target = new Matrix();

		MnistPattern pat = pats.get(0);
		pat.toInputMatrix(input);
		pat.toOutputMatrix(target);
		Matrix output = w.feedForward(input);
//		input.mSub(output, output);
		System.out.println("Output");
		System.out.println(output);

	}
	
	void doIt2() throws Exception {
		List<MnistPattern> pats = MnistData.readMnistSet(false);
		Matrix m = new Matrix(28, 28);
		Matrix max = new Matrix(28, 28);
		max.make0();
		
		for (MnistPattern pat : pats) {
			for (int i = 0; i < pat.image.length; i++)
				m.setVectorItem(i, (int) MathUtil.mapValue(pat.image[i] & 255, 0, 255, 0, 9));
			max.mMax(m, max);
		}
		for (int j = 0; j < max.getSizeY(); j++) {
			for (int i = 0; i < max.getSizeX(); i++)
				System.out.print((int) max.getItem(i, j));
			System.out.println();
		}
	}

	public static void main(String[] args) throws Exception {
		new MyMnistCheckData().doIt();
		System.out.println("Done.");
	}
}
