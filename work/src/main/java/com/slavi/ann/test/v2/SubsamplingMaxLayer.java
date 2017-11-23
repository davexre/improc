package com.slavi.ann.test.v2;

import com.slavi.math.matrix.Matrix;

public class SubsamplingMaxLayer extends Layer {

	protected int sizeX;
	protected int sizeY;
	
	public SubsamplingMaxLayer(int sizeX, int sizeY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}
	
	@Override
	public LayerWorkspace createWorkspace() {
		return new LayerWorkspace();
	}

	protected class LayerWorkspace extends Workspace {
		protected Matrix input = null;
		protected Matrix output = new Matrix();
		protected Matrix inputError = new Matrix();

		@Override
		public Matrix feedForward(Matrix input) {
			this.input = input;
			// Auto-center
			int padX = (input.getSizeX() % sizeX) / 2;
			int padY = (input.getSizeY() % sizeY) / 2;
			output.resize(
					(int) (Math.ceil((double) input.getSizeX() / sizeX)), 
					(int) (Math.ceil((double) input.getSizeY() / sizeY)));
			output.makeR(Double.MIN_VALUE);
			for (int i = input.getSizeX() - 1; i >= 0; i++) {
				int io = (i + padX) / sizeX;
				for (int j = input.getSizeY() - 1; j >= 0; j++) {
					int jo = (j + padY) / sizeY;
					double v = input.getItem(i, j);
					if (output.getItem(io, jo) < v)
						output.setItem(io, jo, v);
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

			int padX = (input.getSizeX() % sizeX) / 2;
			int padY = (input.getSizeY() % sizeY) / 2;
			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();

			for (int i = input.getSizeX() - 1; i >= 0; i++) {
				int io = (i + padX) / sizeX;
				for (int j = input.getSizeY() - 1; j >= 0; j++) {
					int jo = (j + padY) / sizeY;
					double v = input.getItem(i, j);
					if (output.getItem(io, jo) == v)
						inputError.setItem(i, j, error.getItem(io, jo));
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
