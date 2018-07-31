package com.slavi.ann.test.v2.connection;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

public class ConvolutionWithStrideLayer extends Layer {
	public Matrix kernel;
	public double learningRate;
	public int strideX;
	public int strideY;

	public ConvolutionWithStrideLayer(int kernelSizeX, int kernelSizeY, int strideX, int strideY, double learningRate) {
		this.learningRate = learningRate;
		this.strideX = strideX;
		this.strideY = strideY;
		kernel = new Matrix(kernelSizeX, kernelSizeY);
		fillKernelMatrix(kernel, ConvolutionLayer.kernelSigma);
	}

	public LayerParameters getLayerParams(LayerParameters inputLayerParameters) {
		int sizeOX = (int) Math.ceil(((double) inputLayerParameters.outputSize[0] / strideX));
		int sizeOY = (int) Math.ceil(((double) inputLayerParameters.outputSize[1] / strideY));
		return new LayerParameters(
				new int[] { sizeOX, sizeOY },
				kernel.getVectorSize());
	}

	@Override
	public Workspace createWorkspace() {
		return new Workspace();
	}

	@Override
	public void applyWorkspace(LayerWorkspace workspace) {
		Workspace ws = (Workspace) workspace;
		ws.dKernel.mSum(kernel, kernel);
		ws.resetEpoch();
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append(String.format("learning rate: %.4f, stride (X): %d, (Y): %d\n", learningRate, strideX, strideY))
				.append("kernel\n").append(kernel)
				.toString();
	}

	public class Workspace extends LayerWorkspace {
		public Matrix input;
		public Matrix inputError;
		public Matrix output;

		public Matrix dKernel;

		protected Workspace() {
			int kernelSizeX = kernel.getSizeX();
			int kernelSizeY = kernel.getSizeY();
			dKernel = new Matrix(kernelSizeX, kernelSizeY);
			output = new Matrix();
			input = null;
			inputError = new Matrix();
		}

		@Override
		public Matrix feedForward(Matrix input) {
			this.input = input;
			int sizeOX = (int) Math.ceil(((double) input.getSizeX() / strideX));
			int sizeOY = (int) Math.ceil(((double) input.getSizeY() / strideY));
			int padX = (kernel.getSizeX() - strideX) / 2;
			int padY = (kernel.getSizeY() - strideY) / 2;
			output.resize(sizeOX, sizeOY);
			for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
				for (int ox = output.getSizeX() - 1; ox >= 0; ox--) {
					double r = 0.0;
					for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
						int iy = oy * strideY + ky - padY;
						if (iy < 0 || iy >= input.getSizeY())
							continue;
						for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
							int ix = ox * strideY + kx - padX;
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
		public Matrix backPropagate(Matrix error) {
			if (input == null)
				throw new Error("Invalid state");
			if ((output.getSizeX() != error.getSizeX()) ||
				(output.getSizeY() != error.getSizeY()))
				throw new Error("Invalid argument");

			int padX = (kernel.getSizeX() - strideX) / 2;
			int padY = (kernel.getSizeY() - strideY) / 2;
			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();
			for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
				for (int ox = output.getSizeX() - 1; ox >= 0; ox--) {
					double r = error.getItem(ox, oy);
					for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
						int iy = oy * strideY + ky - padY;
						if (iy < 0 || iy >= input.getSizeY())
							continue;
						for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
							int ix = ox * strideY + kx - padX;
							if (ix < 0 || ix >= input.getSizeX())
								continue;
							double dw = r * input.getItem(ix, iy) * learningRate;
							inputError.itemAdd(ix, iy, r * kernel.getItem(kx, ky));
							dKernel.itemAdd(kx, ky, -dw); // the w-dw mean descent, while w+dw means ascent (maximize the error)
						}
					}

				}
			}
			return inputError;
		}

		@Override
		protected void resetEpoch() {
			dKernel.make0();
		}

		public String toString() {
			return new StringBuilder()
					.append("Kernel\n").append(kernel)
					.append("dKernel\n").append(dKernel)
					.append("output\n").append(output)
					.toString();
		}
	}
}
