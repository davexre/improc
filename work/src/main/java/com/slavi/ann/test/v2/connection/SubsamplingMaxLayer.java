package com.slavi.ann.test.v2.connection;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

public class SubsamplingMaxLayer extends Layer {

	public int sizeX;
	public int sizeY;

	protected SubsamplingMaxLayer() {}

	public SubsamplingMaxLayer(int sizeX, int sizeY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	@Override
	public int[] getOutputSize(int inputSize[]) {
		int sizeOX = (int) Math.ceil(((double) inputSize[0] / sizeX));
		int sizeOY = (int) Math.ceil(((double) inputSize[1] / sizeY));
		return new int[] { sizeOX, sizeOY };
	}

	@Override
	public Workspace createWorkspace() {
		return new Workspace();
	}

	@Override
	public String toString() {
		return String.format("size (X): %d, (Y): %d", sizeX, sizeY);
	}

	public class Workspace extends LayerWorkspace {
		public Matrix input = null;
		public Matrix output = new Matrix();
		public Matrix inputError = new Matrix();

		@Override
		public Matrix feedForward(Matrix input) {
			this.input = input;
			int sizeOX = (int) Math.ceil(((double) input.getSizeX() / sizeX));
			int sizeOY = (int) Math.ceil(((double) input.getSizeY() / sizeY));
			int padX = (input.getSizeX() % sizeX) / 2;
			int padY = (input.getSizeY() % sizeY) / 2;
			output.resize(sizeOX, sizeOY);
			output.makeR(Double.MIN_VALUE);
			for (int ix = input.getSizeX() - 1; ix >= 0; ix--) {
				int ox = (ix - padX) / sizeX;
				for (int iy = input.getSizeY() - 1; iy >= 0; iy--) {
					int oy = (iy - padY) / sizeY;
					double v = input.getItem(ix, iy);
					if (output.getItem(ox, oy) < v)
						output.setItem(ox, oy, v);
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

			int padX = (input.getSizeX() % sizeX) / 2;
			int padY = (input.getSizeY() % sizeY) / 2;
			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();

			for (int ix = input.getSizeX() - 1; ix >= 0; ix--) {
				int ox = (ix - padX) / sizeX;
				for (int iy = input.getSizeY() - 1; iy >= 0; iy--) {
					int oy = (iy - padY) / sizeY;
					double v = input.getItem(ix, iy);
					if (output.getItem(ox, oy) == v)
						inputError.setItem(ix, iy, error.getItem(ox, oy));
				}
			}
			input = null;
			return inputError;
		}
	}
}
