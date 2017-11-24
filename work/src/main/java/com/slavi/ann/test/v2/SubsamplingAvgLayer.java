package com.slavi.ann.test.v2;

import com.slavi.math.matrix.Matrix;

public class SubsamplingAvgLayer extends Layer {

	public int sizeX;
	public int sizeY;
	
	public SubsamplingAvgLayer(int sizeX, int sizeY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}
	
	@Override
	public LayerWorkspace createWorkspace() {
		return new LayerWorkspace();
	}

	public class LayerWorkspace extends Workspace {
		public Matrix input = null;
		public Matrix output = new Matrix();
		public Matrix inputError = new Matrix();

		@Override
		public Matrix feedForward(Matrix input) {
			this.input = input;
			// Auto-center
			int padX = (input.getSizeX() % sizeX) / 2;
			int padY = (input.getSizeY() % sizeY) / 2;
			output.resize(
					(int) (Math.ceil((double) input.getSizeX() / sizeX)), 
					(int) (Math.ceil((double) input.getSizeY() / sizeY)));
			output.make0();
			for (int i = input.getSizeX() - 1; i >= 0; i++) {
				int io = (i + padX) / sizeX;
				for (int j = input.getSizeY() - 1; j >= 0; j++) {
					int jo = (j + padY) / sizeY;
					output.itemAdd(io, jo, input.getItem(i, j));
				}
			}
			output.rMul(sizeX * sizeY);
			return output;
		}

		@Override
		public Matrix backPropagate(Matrix error) {
			if (input == null)
				throw new Error("Invalid state");
			if ((output.getSizeX() != error.getSizeX()) ||
				(output.getSizeY() != error.getSizeY()))
				throw new Error("Invalid argument");

			int padX = (input.getSizeX() % sizeX) / 2;
			int padY = (input.getSizeY() % sizeY) / 2;
			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();

			double scale = 1.0 / (sizeX * sizeY);
			for (int i = input.getSizeX() - 1; i >= 0; i++) {
				int io = (i + padX) / sizeX;
				for (int j = input.getSizeY() - 1; j >= 0; j++) {
					int jo = (j + padY) / sizeY;
					inputError.setItem(i, j, scale * input.getItem(i, j) * error.getItem(io, jo));
				}
			}
			input = null;
			return inputError;
		}

		@Override
		protected void resetEpoch() {
		}
	}
}
