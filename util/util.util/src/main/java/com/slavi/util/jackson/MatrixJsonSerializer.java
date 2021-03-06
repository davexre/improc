package com.slavi.util.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.slavi.math.matrix.Matrix;

public class MatrixJsonSerializer extends StdSerializer<Matrix> {
	public MatrixJsonSerializer() {
		super(Matrix.class);
	}

	@Override
	public void serialize(Matrix value, JsonGenerator g, SerializerProvider provider)
			throws IOException {
		g.writeStartArray();
		double r[] = new double[value.getSizeX()];
		for (int j = 0; j < value.getSizeY(); j++) {
			for (int i = 0; i < value.getSizeX(); i++)
				r[i] = value.getItem(i, j);
			g.writeArray(r, 0, r.length);
		}
		g.writeEndArray();
	}
}
