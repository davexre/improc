package com.slavi.ann.test.v2.activation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.slavi.ann.test.v2.Layer;
import com.slavi.jackson.StatisticsFormatJsonConverter;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class DebugLayer extends Layer {

	public static int defaultStyle = Statistics.CStatCount | Statistics.CStatAvg | Statistics.CStatStdDev | Statistics.CStatMinMax | Statistics.CStatAbs;
	public static int off = 0;

	String name;

	@JsonSerialize(converter = StatisticsFormatJsonConverter.Serialize.class)
	@JsonDeserialize(converter = StatisticsFormatJsonConverter.Deserialize.class)
	int inputStyle;

	@JsonSerialize(converter = StatisticsFormatJsonConverter.Serialize.class)
	@JsonDeserialize(converter = StatisticsFormatJsonConverter.Deserialize.class)
	int errorStyle;

	public static String styleToString(int style) {
		StringBuilder r = new StringBuilder();
		if (style == Statistics.CStatAll) return "ALL";

		if ((style & Statistics.CStatCount ) != 0) r.append("Count ");
		if ((style & Statistics.CStatAvg   ) != 0) r.append("Avg ");
		if ((style & Statistics.CStatJ     ) != 0) r.append("J ");
		if ((style & Statistics.CStatAE    ) != 0) r.append("AE ");
		if ((style & Statistics.CStatMinMax) != 0) r.append("MinMax ");
		if ((style & Statistics.CStatAbs   ) != 0) r.append("Abs ");
		if ((style & Statistics.CStatDelta ) != 0) r.append("Delta ");
		if ((style & Statistics.CStatMD    ) != 0) r.append("MD ");
		if ((style & Statistics.CStatStdDev) != 0) r.append("StdDev ");
		if ((style & Statistics.CStatErrors) != 0) r.append("Err ");

		return r.toString().trim();
	}

	public static boolean hasBadValues(Matrix m) {
		for (int i = m.getVectorSize() - 1; i >= 0; i--)
			if (!Double.isFinite(m.getVectorItem(i)))
				return true;
		return false;
	}

	protected DebugLayer() {}

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

	@Override
	public String toString() {
		return String.format("name: %s, style (in): %s, (err): %s", name, styleToString(inputStyle), styleToString(errorStyle));
	}

	public class Workspace extends LayerWorkspace {
		public MatrixStatistics stInput = new MatrixStatistics();
		public MatrixStatistics stError = new MatrixStatistics();
		public boolean hasBadValuesInInput = false;
		public boolean hasBadValuesInError = false;

		@Override
		public Matrix feedForward(Matrix input) {
			if (hasBadValues(input))
				hasBadValuesInInput = true;
			stInput.addValue(input);
			return input;
		}

		@Override
		public Matrix backPropagate(Matrix coefs, int startingIndex, Matrix error) {
			if (hasBadValues(error))
				hasBadValuesInError = true;
			stError.addValue(error);
			return error;
		}

		@Override
		protected void resetEpoch() {
			stInput.stop();
			stError.stop();

			if (inputStyle != 0 || errorStyle != 0 || hasBadValuesInInput || hasBadValuesInError)
				System.out.println(">>> Debug " + name);
			if (inputStyle != 0) {
				System.out.println("Input Stats");
				//System.out.println(stInput.toString(inputStyle));
				System.out.println(stInput.statStatToString(inputStyle));
			}
			if (errorStyle != 0) {
				System.out.println("Error Stats");
				//System.out.println(stError.toString(errorStyle));
				System.out.println(stError.statStatToString(errorStyle));
			}

			if (hasBadValuesInInput) System.out.println("HAS BAD VALUES IN INPUT");
			if (hasBadValuesInError) System.out.println("HAS BAD VALUES IN ERROR");
			hasBadValuesInInput = false;
			hasBadValuesInError = false;
			stInput.start();
			stError.start();
		};
	}
}
