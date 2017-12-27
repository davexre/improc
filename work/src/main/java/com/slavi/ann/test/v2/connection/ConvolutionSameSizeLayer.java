package com.slavi.ann.test.v2.connection;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

public class ConvolutionSameSizeLayer extends Layer {

	public Matrix kernel;
	public double learningRate;

	public ConvolutionSameSizeLayer(int kernelSizeX, int kernelSizeY, double learningRate) {
		this.learningRate = learningRate;
		kernel = new Matrix(kernelSizeX, kernelSizeY);
		fillKernelMatrix(kernel, 0.3);
	}
	
	public int[] getOutputSize(int inputSize[]) {
		return new int[] { inputSize[0], inputSize[1] };
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
			output.resize(input.getSizeX(), input.getSizeY());
			// See https://octave.sourceforge.io/octave/function/conv2.html
			int padX = kernel.getSizeX() / 2;
			int padY = kernel.getSizeY() / 2;

			for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
				for (int ox = output.getSizeX() - 1; ox >= 0; ox--) {
					double r = 0.0;
					for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
						int iy = oy + ky - padY;
						if (iy < 0 || iy >= input.getSizeY())
							continue;
						for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
							int ix = ox + kx - padX;
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

			int padX = kernel.getSizeX() / 2;
			int padY = kernel.getSizeY() / 2;
			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();
			for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
				for (int ox = output.getSizeX() - 1; ox >= 0; ox--) {
					double r = error.getItem(ox, oy);
					for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
						int iy = oy + ky - padY;
						if (iy < 0 || iy >= input.getSizeY())
							continue;
						for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
							int ix = ox + kx - padX;
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
