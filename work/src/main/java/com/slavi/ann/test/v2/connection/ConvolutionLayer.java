package com.slavi.ann.test.v2.connection;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

public class ConvolutionLayer extends Layer {
	public static final double kernelSigma = 1.5;

	public Matrix kernel;
	public double learningRate;

	protected ConvolutionLayer() {}

	public ConvolutionLayer(int kernelSizeX, int kernelSizeY, double learningRate) {
		this.learningRate = learningRate;
		kernel = new Matrix(kernelSizeX, kernelSizeY);
		fillKernelMatrix(kernel, kernelSigma);
	}

	@Override
	public int[] getOutputSize(int inputSize[]) {
		int sizeOX = (int) Math.ceil(((double) inputSize[0] / kernel.getSizeX()));
		int sizeOY = (int) Math.ceil(((double) inputSize[1] / kernel.getSizeY()));
		return new int[] { sizeOX, sizeOY };
	}

	@Override
	public void extractParams(Matrix delta, int startingIndex) {
		for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
			int coefIndex = startingIndex + ky * kernel.getSizeX();
			for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
				delta.setItem(coefIndex + kx, 0, kernel.getItem(kx, ky));
			}
		}
	}

	@Override
	public void loadParams(Matrix delta, int startingIndex) {
		for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
			int coefIndex = startingIndex + ky * kernel.getSizeX();
			for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
				double r = delta.getItem(coefIndex + kx, 0);
				kernel.setItem(kx, ky, r);
			}
		}
	}

	@Override
	public int getNumAdjustableParams() {
		return kernel.getVectorSize();
	};

	@Override
	public Workspace createWorkspace() {
		return new Workspace();
	}

	@Override
	public void applyWorkspace(LayerWorkspace workspace) {
		Workspace ws = (Workspace) workspace;
		ws.dKernel.mSum(kernel, kernel);
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append(String.format("learning rate: %.4f\n", learningRate))
				.append("kernel\n").append(kernel)
				.toString();
	}

	public class Workspace extends LayerWorkspace {
		public Matrix input;
		public Matrix inputError;
		public Matrix output;
		public Matrix outputError;	// Used by com.slavi.ann.test.v2.Utils.DrawConvolutionLayer
		public Matrix dKernel;

		protected Workspace() {
			int kernelSizeX = kernel.getSizeX();
			int kernelSizeY = kernel.getSizeY();
			dKernel = new Matrix(kernelSizeX, kernelSizeY);
			output = new Matrix();
			outputError = new Matrix();
			input = null;
			inputError = new Matrix();
		}

		@Override
		public Matrix feedForward(Matrix input) {
			this.input = input;
			int sizeOX = (int) Math.ceil(((double) input.getSizeX() / kernel.getSizeX()));
			int sizeOY = (int) Math.ceil(((double) input.getSizeY() / kernel.getSizeY()));
			int padX = (input.getSizeX() % kernel.getSizeX()) / 2;
			int padY = (input.getSizeY() % kernel.getSizeY()) / 2;
			output.resize(sizeOX, sizeOY);
			outputError.resize(sizeOX, sizeOY);
			for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
				for (int ox = output.getSizeX() - 1; ox >= 0; ox--) {
					double r = 0.0;
					for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
						int iy = oy * kernel.getSizeY() + ky - padY;
						if (iy < 0 || iy >= input.getSizeY())
							continue;
						for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
							int ix = ox * kernel.getSizeX() + kx - padX;
							if (ix < 0 || ix >= input.getSizeX())
								continue;
							r += input.getItem(ix, iy) * kernel.getItem(kx, ky);
						}
					}
					output.setItem(ox, oy, r);
				}
			}
			return output;
		}

		@Override
		public Matrix backPropagate(Matrix coefs, int startingIndex, Matrix error) {
			if (input == null)
				throw new Error("Invalid state");
			if ((output.getSizeX() != error.getSizeX()) ||
				(output.getSizeY() != error.getSizeY()))
				throw new Error("Invalid argument");

			outputError.mMaxAbs(error, outputError);
			int padX = (input.getSizeX() % kernel.getSizeX()) / 2;
			int padY = (input.getSizeY() % kernel.getSizeY()) / 2;
			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();
			for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
				for (int ox = output.getSizeX() - 1; ox >= 0; ox--) {
					double r = error.getItem(ox, oy) * learningRate;
					for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
						int iy = oy * kernel.getSizeY() + ky - padY;
						if (iy < 0 || iy >= input.getSizeY())
							continue;
						int coefIndex = startingIndex + ky * kernel.getSizeX();
						for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
							int ix = ox * kernel.getSizeX() + kx - padX;
							if (ix < 0 || ix >= input.getSizeX())
								continue;
							double dw = r * input.getItem(ix, iy);
							inputError.itemAdd(ix, iy, r * kernel.getItem(kx, ky));
							dKernel.itemAdd(kx, ky, -dw); // the w-dw mean descent, while w+dw means ascent (maximize the error)
							coefs.itemAdd(coefIndex + kx, 0, -dw);
						}
					}
				}
			}
			return inputError;
		}

		@Override
		protected void resetEpoch() {
			dKernel.make0();
			outputError.make0();
		}

		@Override
		public String toString() {
			return new StringBuilder()
					.append("Kernel\n").append(kernel)
					.append("dKernel\n").append(dKernel)
					.append("output\n").append(output)
					.toString();
		}
	}
}
