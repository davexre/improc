package com.slavi.ann.test.dataset;

import java.util.ArrayList;
import java.util.List;

import com.slavi.ann.test.DatapointPair;
import com.slavi.math.matrix.Matrix;

public class BinaryDigits {

	public static class BinaryDigitsPattern implements DatapointPair {
		final int number;

		public BinaryDigitsPattern(int number) {
			this.number = number;
		}

		public void toInputMatrix(Matrix dest) {
			dest.resize(4, 4);
			for (int i = 0; i < dest.getVectorSize(); i++)
				dest.setVectorItem(i, number == i ? 0.95 : 0.05);
		}

		public void toOutputMatrix(Matrix dest) {
			dest.resize(4, 1);
			for (int i = 0; i < dest.getVectorSize(); i++)
				dest.setVectorItem(i, (number & (1 << i)) == 0 ? 0.05 : 0.95);
		}

		public String getName() {
			return Integer.toString(number);
		}
	}

	public static List<BinaryDigitsPattern> dataSet() {
		ArrayList<BinaryDigitsPattern> trainset = new ArrayList<>();
		for (int i = 0; i < 16; i++)
			trainset.add(new BinaryDigitsPattern(i));
		return trainset;
	}
}
