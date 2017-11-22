package com.slavi.ann.test.v2;

import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class ConvolutionLayer extends Layer {

	protected Matrix kernel;
	protected double learningRate;

	public ConvolutionLayer(int kernelSizeX, int kernelSizeY, double learningRate) {
		this.learningRate = learningRate;
		kernel = new Matrix(kernelSizeX, kernelSizeY);
	}
	
	@Override
	public LayerWorkspace createWorkspace() {
		return new LayerWorkspace();
	}

	@Override
	public void applyWorkspace(Workspace workspace) {
		LayerWorkspace ws = (LayerWorkspace) workspace;
		ws.ms.stop();
		System.out.println(ws.ms.toString(Statistics.CStatStdDev | Statistics.CStatMinMax));
		ws.dKernel.mSum(kernel, kernel);
		ws.resetEpoch();
	}

	protected class LayerWorkspace extends Workspace {
		protected Matrix input;
		protected Matrix inputError;
		protected Matrix output;
		protected Matrix dKernel;
		protected Matrix dKernel1;
		protected MatrixStatistics ms = new MatrixStatistics();

		protected LayerWorkspace() {
			int kernelSizeX = kernel.getSizeX();
			int kernelSizeY = kernel.getSizeY();
			dKernel = new Matrix(kernelSizeX, kernelSizeY);
			dKernel1 = new Matrix(kernelSizeX, kernelSizeY);
			output = new Matrix();
			input = null;
			inputError = new Matrix();
		}

		@Override
		public Matrix feedForward(Matrix input) {
			this.input = input;
			output.resize(input.getSizeX() - kernel.getSizeX() + 1, input.getSizeY() - kernel.getSizeY() + 1);
			for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
				for (int ox = output.getSizeY() - 1; ox >= 0; ox--) {
					double r = 0.0;
					for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
						for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
							r += input.getItem(ox + kx, oy + ky) * kernel.getItem(kx, ky);
						}
					}
					r = 1.0 / (1.0 + Math.exp(-r));
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

			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();
			dKernel1.make0();
			for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
				for (int ox = output.getSizeY() - 1; ox >= 0; ox--) {
					double r = output.getItem(ox, oy);
					r = error.getItem(ox, oy) * r * (1 - r);
					for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
						for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
							double dw = r * input.getItem(ox + kx, oy + ky) * learningRate;
							inputError.itemAdd(ox + kx, oy + ky, r * kernel.getItem(kx, ky));
							dKernel1.itemAdd(kx, ky, -dw); // the w-dw mean descent, while w+dw means ascent (maximize the error)
						}
					}

				}
			}
			dKernel.mSum(dKernel1, dKernel);
//			dKernel1.termAbs(dKernel1);
			Matrix m = new Matrix();
			error.termAbs(m);
			ms.addValue(m);
			return inputError;
		}

		@Override
		protected void resetEpoch() {
			dKernel.make0();
			ms.start();
		}
	}
}
