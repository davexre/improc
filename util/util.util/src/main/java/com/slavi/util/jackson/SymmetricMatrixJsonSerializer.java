package com.slavi.util.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.slavi.math.matrix.SymmetricMatrix;

public class SymmetricMatrixJsonSerializer extends StdSerializer<SymmetricMatrix> {
	public SymmetricMatrixJsonSerializer() {
		super(SymmetricMatrix.class);
	}

	@Override
	public void serialize(SymmetricMatrix value, JsonGenerator g, SerializerProvider provider)
			throws IOException {
		g.writeStartArray();
		double r[] = new double[value.getSizeY()];
		for (int j = 0; j < value.getSizeY(); j++) {
			for (int i = 0; i <= j; i++)
				r[i] = value.getItem(i, j);
			g.writeArray(r, 0, j + 1);
		}
		g.writeEndArray();
	}
}
