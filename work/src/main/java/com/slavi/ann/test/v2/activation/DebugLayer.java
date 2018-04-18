package com.slavi.ann.test.v2.activation;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class DebugLayer extends Layer {
	
	public static int defaultStyle = Statistics.CStatAvg | Statistics.CStatStdDev | Statistics.CStatMinMax | Statistics.CStatAbs;
	public static int off = 0;
	
	String name;
	int inputStyle;
	int errorStyle;
	
	public DebugLayer(String name) {
		this(name, defaultStyle);
	}
	
	public DebugLayer(String name, int style) {
		this(name, style, style);
	}
	
	public DebugLayer(String name, int inputStyle, int errorStyle) {
		this.name = name;
		this.inputStyle = inputStyle;
		this.errorStyle = errorStyle;
	}

	@Override
	public int[] getOutputSize(int[] inputSize) {
		return inputSize;
	}

	@Override
	public LayerWorkspace createWorkspace() {
		return new Workspace();
	}
	
	public class Workspace extends LayerWorkspace {
		public MatrixStatistics stInput = new MatrixStatistics();
		public MatrixStatistics stError = new MatrixStatistics();
		
		@Override
		public Matrix feedForward(Matrix input) {
			stInput.addValue(input);
			return input;
		}

		@Override
		public Matrix backPropagate(Matrix error) {
			stError.addValue(error);
			return error;
		}

		@Override
		protected void resetEpoch() {
			stInput.stop();
			stError.stop();

			if (inputStyle != 0 || errorStyle != 0)
				System.out.println("Debug " + name);
			if (inputStyle != 0) {
				System.out.println("Input Stats");
				System.out.println(stInput.toString(inputStyle));
			}
			if (errorStyle != 0) {
				System.out.println("Error Stats");
				System.out.println(stError.toString(errorStyle));
			}
			
			stInput.start();
			stError.start();
		};
	}
}
