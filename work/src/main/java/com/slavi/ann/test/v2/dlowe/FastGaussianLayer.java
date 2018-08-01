package com.slavi.ann.test.v2.dlowe;

import com.slavi.ann.test.v2.Layer;
import com.slavi.improc.parallel.PGaussianFilter;
import com.slavi.math.matrix.Matrix;

public class FastGaussianLayer extends Layer {

	double sigma;
	int maskRadius;
	double mask[];

	public FastGaussianLayer() {
		this(PGaussianFilter.defaultSigma, PGaussianFilter.getMaskRadius(PGaussianFilter.defaultSigma));
	}

	public FastGaussianLayer(double sigma, int maskRadius) {
		this.sigma = sigma;
		this.maskRadius = maskRadius;
		this.mask = new double[(maskRadius << 1) - 1];
		PGaussianFilter.fillArray(mask, sigma);
	}

	@Override
	public int[] getOutputSize(int inputSize[]) {
		return inputSize;
	}

	@Override
	public Workspace createWorkspace() {
		return new Workspace();
	}

	@Override
	public String toString() {
		return String.format("R: %d, sigma: %.4f\n", maskRadius, sigma);
	}

	public class Workspace extends LayerWorkspace {
		public Matrix input;
		public Matrix inputError;
		public Matrix output;
		double buf[] = null;

		protected Workspace() {
			input = null;
			inputError = new Matrix();
			output = new Matrix();
		}

		@Override
		public Matrix feedForward(Matrix input) {
			this.input = input;
			output.resize(input.getSizeX(), input.getSizeY());
			int maxBufSize = Math.max(input.getSizeX(), input.getSizeY()) + mask.length - 1;
			if (buf == null || buf.length < maxBufSize)
				buf = new double[maxBufSize];

			for (int destI = input.getSizeX() - 1; destI >= 0; destI--) {
				// fill in the buffer
				int bufIndex = 0;
				double tmp = input.getItem(destI, 0);
				for (int j = 1; j < maskRadius; j++)
					buf[bufIndex++] = tmp;
				for (int j = 0; j < input.getSizeY(); j++)
					buf[bufIndex++] = input.getItem(destI, j);
				tmp = buf[bufIndex - 1];
				for (int j = 1; j < maskRadius; j++)
					buf[bufIndex++] = tmp;
				// apply mask
				bufIndex--;
				for (int j = input.getSizeY() - 1; j >= 0; j--, bufIndex--) {
					double sum = 0;
					for (int k = mask.length - 1; k >= 0; k--)
						sum += mask[k] * buf[bufIndex - k];
					output.setItem(destI, j, sum);
				}
			}

			for (int destJ = input.getSizeY() - 1; destJ >= 0; destJ--) {
				// fill in the buffer
				int bufIndex = 0;
				double tmp = output.getItem(0, destJ);
				for (int i = 1; i < maskRadius; i++)
					buf[bufIndex++] = tmp;
				for (int i = 0; i < input.getSizeX(); i++)
					buf[bufIndex++] = output.getItem(i, destJ);
				tmp = buf[bufIndex - 1];
				for (int i = 1; i < maskRadius; i++)
					buf[bufIndex++] = tmp;
				// apply mask
				bufIndex--;
				for (int i = input.getSizeX() - 1; i >= 0; i--, bufIndex--) {
					double sum = 0;
					for (int k = mask.length - 1; k >= 0; k--)
						sum += mask[k] * buf[bufIndex - k];
					output.setItem(i, destJ, sum);
				}
			}
			return output;
		}

/*
	f = sum(mask * X)
	df/dX = ??

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

			// TODO: imlement

			for (int destI = input.getSizeX() - 1; destI >= 0; destI--) {
				// fill in the buffer
				int bufIndex = 0;
				double tmp = error.getItem(destI, 0);
				for (int j = 1; j < maskRadius; j++)
					buf[bufIndex++] = tmp;
				for (int j = 0; j < error.getSizeY(); j++)
					buf[bufIndex++] = error.getItem(destI, j);
				tmp = buf[bufIndex - 1];
				for (int j = 1; j < maskRadius; j++)
					buf[bufIndex++] = tmp;
				// apply mask
				bufIndex--;
				for (int j = input.getSizeY() - 1; j >= 0; j--, bufIndex--) {
					double sum = 0;
					for (int k = mask.length - 1; k >= 0; k--)
						sum += mask[k] * buf[bufIndex - k];
					inputError.setItem(destI, j, sum);
				}
			}


			int bottom;
			for (int oy = bottom = output.getSizeY() - 1; oy >= 0; oy--) {
				int top = oy == 0 ? 0 : oy - 1;
				int right;
				for (int ox = right = output.getSizeX() - 1; ox >= 0; ox--) {
					int left = ox == 0 ? 0 : ox - 1;
					double A = input.getItem(ox, bottom) - input.getItem(ox, top);
					double B = input.getItem(right, oy) - input.getItem(left, oy);
					double r = A * A + B * B;
					if (r < 0.000001)
						continue;
					double dfdA = - error.getItem(ox, oy) * B / r;
					double dfdB = error.getItem(ox, oy) * A / r;

					inputError.itemAdd(ox, bottom, dfdA);
					inputError.itemAdd(ox, top, -dfdA);
					inputError.itemAdd(right, oy, dfdB);
					inputError.itemAdd(left, oy, -dfdB);
					right = ox;
				}
				bottom = oy;
			}
			return inputError;
		}
	}

	public static void main(String[] args) {
		double buf[] = new double[5];
		PGaussianFilter.fillArray(buf, PGaussianFilter.defaultSigma);
		double sum = 0;
		for (int i = 0; i < buf.length; i++)
			sum += buf[i];
		System.out.println(sum);
	}

}
