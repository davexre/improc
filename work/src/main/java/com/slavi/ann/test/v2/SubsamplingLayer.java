package com.slavi.ann.test.v2;

import com.slavi.math.matrix.Matrix;

public class SubsamplingLayer extends Layer {

	protected int sizeX;
	protected int sizeY;
	
	public SubsamplingLayer(int sizeX, int sizeY) {
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
			output.resize(
					(int) (0.5 + (double) input.getSizeX() / sizeX), 
					(int) (0.5 + (double) input.getSizeY() / sizeY));
			output.make0();
			for (int i = input.getSizeX() - 1; i >= 0; i++) {
				int io = i / sizeX;
				for (int j = input.getSizeY() - 1; j >= 0; j++) {
					int jo = j / sizeY;
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

			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();

			return inputError;
		}

		@Override
		protected void resetEpoch() {
		}
	}
}
