package com.slavi.ann.test.v2;

import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class ConvolutionWithStrideLayer extends Layer {
	public Matrix kernel;
	public double learningRate;
	public int strideX;
	public int strideY;
	public double scale;
	public double bias;
	
	public ConvolutionWithStrideLayer(int kernelSizeX, int kernelSizeY, int strideX, int strideY, double learningRate) {
		this.learningRate = learningRate;
		this.strideX = strideX;
		this.strideY = strideY;
		kernel = new Matrix(kernelSizeX, kernelSizeY);
		scale = 15.0 / kernel.getVectorSize();
//		bias = 5;
//		scale = 1; // ???
		fillKernelMatrix(kernel, 0.3);
		kernel.rMul(scale);
		scale = 1;
	}
	
	public int[] getOutputSize(int inputSize[]) {
		int sizeOX = (int) Math.ceil(((double) inputSize[0] / strideX));
		int sizeOY = (int) Math.ceil(((double) inputSize[1] / strideY));
		return new int[] { sizeOX, sizeOY };
	}

	@Override
	public LayerWorkspace createWorkspace() {
		return new LayerWorkspace();
	}

	@Override
	public void applyWorkspace(Workspace workspace) {
		LayerWorkspace ws = (LayerWorkspace) workspace;
		ws.dKernel.mSum(kernel, kernel);
		ws.resetEpoch();
	}

	public class LayerWorkspace extends Workspace {
		public Matrix input;
		public Matrix inputError;
		public Matrix output;
		public Matrix output0;
		
		public Matrix dKernel;
		public Matrix dKernel1;
		public MatrixStatistics ms = new MatrixStatistics();

		protected LayerWorkspace() {
			int kernelSizeX = kernel.getSizeX();
			int kernelSizeY = kernel.getSizeY();
			dKernel = new Matrix(kernelSizeX, kernelSizeY);
			dKernel1 = new Matrix(kernelSizeX, kernelSizeY);
			output = new Matrix();
			output0 = new Matrix();
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
			output0.resize(sizeOX, sizeOY);
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
					output0.setItem(ox, oy, bias - r * scale);
					r = 1.0 / (1.0 + Math.exp(bias - r * scale));
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
			dKernel1.make0();
			for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
				for (int ox = output.getSizeX() - 1; ox >= 0; ox--) {
					double r = output.getItem(ox, oy);
					r = scale * error.getItem(ox, oy) * r * (1 - r);
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
							dKernel1.itemAdd(kx, ky, -dw); // the w-dw mean descent, while w+dw means ascent (maximize the error)
						}
					}

				}
			}
//			dKernel1.rMul(1.0 / output.getVectorSize());
			dKernel.mSum(dKernel1, dKernel);
//			dKernel1.termAbs(dKernel1);
			Matrix m = new Matrix();
			error.termAbs(m);
			ms.addValue(m);
			input = null;
			return inputError;
		}

		@Override
		protected void resetEpoch() {
			dKernel.make0();
			ms.start();
		}
		
		public String toString() {
			ms.stop();
			return new StringBuilder()
					.append("INTERNAL STATS (error)\n").append(ms.toString(Statistics.CStatStdDev | Statistics.CStatMinMax))
					.append("Kernel\n").append(kernel)
					.append("dKernel\n").append(dKernel)
					.append("dKernel1\n").append(dKernel1)
					.append("output0\n").append(output0)
					.append("output\n").append(output)
					.toString();
		}
	}
}
