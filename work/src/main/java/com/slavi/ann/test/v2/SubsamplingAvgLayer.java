package com.slavi.ann.test.v2;

import com.slavi.math.matrix.Matrix;

public class SubsamplingAvgLayer extends Layer {

	protected int sizeX;
	protected int sizeY;
	protected int strideX;
	protected int strideY;
	
	public SubsamplingAvgLayer(int sizeX, int sizeY, int strideX, int strideY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.strideX = strideX;
		this.strideY = strideY;
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
					(int) (Math.ceil((double) input.getSizeX() / strideX)), 
					(int) (Math.ceil((double) input.getSizeY() / strideY)));
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
