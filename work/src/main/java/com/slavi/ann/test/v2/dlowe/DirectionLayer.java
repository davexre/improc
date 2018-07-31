package com.slavi.ann.test.v2.dlowe;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

public class DirectionLayer extends Layer {

	@Override
	public LayerParameters getLayerParams(LayerParameters inputLayerParameters) {
		return inputLayerParameters;
	}

	@Override
	public Workspace createWorkspace() {
		return new Workspace();
	}

	public class Workspace extends LayerWorkspace {
		public Matrix input;
		public Matrix inputError;
		public Matrix output;

		protected Workspace() {
			input = null;
			inputError = new Matrix();
			output = new Matrix();
		}

		@Override
		public Matrix feedForward(Matrix input) {
			this.input = input;
			output.resize(input.getSizeX(), input.getSizeY());

			int bottom;
			for (int oy = bottom = output.getSizeY() - 1; oy >= 0; oy--) {
				int top = oy == 0 ? 0 : oy - 1;
				int right;
				for (int ox = right = output.getSizeX() - 1; ox >= 0; ox--) {
					int left = ox == 0 ? 0 : ox - 1;
					// Direction is computed as d = atan2( dX, dY )
					// The returned value of atan2 is from -pi to +pi.
					double f = Math.atan2(
						input.getItem(ox, bottom) - input.getItem(ox, top),
						input.getItem(right, oy) - input.getItem(left, oy));
					output.setItem(ox, oy, f);
					right = ox;
				}
				bottom = oy;
			}
			return output;
		}

/*
(atan2(X,Y))'	=> d(atan2)/dX = -Y/(X^2 + Y^2);  d(atan2)/dY = X/(X^2 + Y^2)
https://en.wikipedia.org/wiki/Atan2#Derivative

f = atan2(A, B) = atan(A/B)
df/dA = -B/(A*A+B*B)
df/dB = A/(A*A+B*B)

X - input
A = X(ox, bottom) - X(ox, top)
B = X(right, oy) - X(left, oy)

dA/dX(ox, bottom) = 1
dA/dX(ox, top) = -1
dB/dX(right, oy) = 1
dB/dX(left, oy) = -1
*/
		@Override
		public Matrix backPropagate(Matrix error) {
			if (input == null)
				throw new Error("Invalid state");
			if ((input.getSizeX() != error.getSizeX()) ||
				(input.getSizeY() != error.getSizeY()))
				throw new Error("Invalid argument");

			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();

			int bottom;
			for (int oy = bottom = output.getSizeY() - 1; oy >= 0; oy--) {
				int top = oy == 0 ? 0 : oy - 1;
				int right;
				for (int ox = right = output.getSizeX() - 1; ox >= 0; ox--) {
					int left = ox == 0 ? 0 : ox - 1;
					double A = input.getItem(ox, bottom) - input.getItem(ox, top);
					double B = input.getItem(right, oy) - input.getItem(left, oy);
					double r = A * A + B * B;
					if (r < 0.000001)
						continue;
					double dfdA = - error.getItem(ox, oy) * B / r;
					double dfdB = error.getItem(ox, oy) * A / r;

					inputError.itemAdd(ox, bottom, dfdA);
					inputError.itemAdd(ox, top, -dfdA);
					inputError.itemAdd(right, oy, dfdB);
					inputError.itemAdd(left, oy, -dfdB);
					right = ox;
				}
				bottom = oy;
			}
			return inputError;
		}
	}
}
