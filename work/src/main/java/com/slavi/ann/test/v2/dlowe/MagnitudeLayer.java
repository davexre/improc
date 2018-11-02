package com.slavi.ann.test.v2.dlowe;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

public class MagnitudeLayer extends Layer {

	@Override
	public int[] getOutputSize(int inputSize[]) {
		return inputSize;
	}

	@Override
	public Workspace createWorkspace() {
		return new Workspace();
	}

	public class Workspace extends LayerWorkspace {
		public Matrix input;
		public Matrix inputError;
		public Matrix output;

		protected Workspace() {
			input = null;
			inputError = new Matrix();
			output = new Matrix();
		}

		@Override
		public Matrix feedForward(Matrix input) {
			this.input = input;
			output.resize(input.getSizeX(), input.getSizeY());

			int bottom;
			for (int oy = bottom = output.getSizeY() - 1; oy >= 0; oy--) {
				int top = oy == 0 ? 0 : oy - 1;
				int right;
				for (int ox = right = output.getSizeX() - 1; ox >= 0; ox--) {
					int left = ox == 0 ? 0 : ox - 1;
					double A = input.getItem(right, oy) - input.getItem(left, oy);
					double B = input.getItem(ox, bottom) - input.getItem(ox, top);
					double r = Math.sqrt(A * A + B * B);
					output.setItem(ox, oy, r);
					right = ox;
				}
				bottom = oy;
			}
			return output;
		}

/*
X - input
f = sqrt(A^2 + B^2)
T = A^2 + B^2
df/dT = 1 / (2 * f)

dT/dA = 2*A
dT/dB = 2*B

A = X(right, oy) - X(left, oy)
B = X(ox, bottom) - X(ox, top)

dA/dX(right, oy) = 1
dA/dX(left, oy) = -1
dB/dX(ox, bottom) = 1
dB/dX(ox, top) = -1
*/
		@Override
		public Matrix backPropagate(Matrix coefs, int startingIndex, Matrix error) {
			if (input == null)
				throw new Error("Invalid state");
			if ((input.getSizeX() != error.getSizeX()) ||
				(input.getSizeY() != error.getSizeY()))
				throw new Error("Invalid argument");

			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();

			int bottom;
			for (int oy = bottom = output.getSizeY() - 1; oy >= 0; oy--) {
				int top = oy == 0 ? 0 : oy - 1;
				int right;
				for (int ox = right = output.getSizeX() - 1; ox >= 0; ox--) {
					int left = ox == 0 ? 0 : ox - 1;
					double A = input.getItem(right, oy) - input.getItem(left, oy);
					double B = input.getItem(ox, bottom) - input.getItem(ox, top);
					double dfdT_2 = error.getItem(ox, oy) * output.getItem(ox, oy);
					double dfdA = dfdT_2 * A;
					double dfdB = dfdT_2 * B;
					inputError.itemAdd(right, oy, dfdA);
					inputError.itemAdd(left, oy, -dfdA);
					inputError.itemAdd(ox, bottom, dfdB);
					inputError.itemAdd(ox, top, -dfdB);
					right = ox;
				}
				bottom = oy;
			}
			return inputError;
		}
	}
}
