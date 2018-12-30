package com.slavi.util.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.slavi.math.matrix.Matrix;

public class MatrixXmlSerializer extends StdSerializer<Matrix> {
	public MatrixXmlSerializer() {
		super(Matrix.class);
	}

	@Override
	public void serialize(Matrix value, JsonGenerator g, SerializerProvider provider)
			throws IOException {
		g.writeStartObject();
		for (int j = 0; j < value.getSizeY(); j++) {
			g.writeFieldName("r");
			g.writeStartObject();
			for (int i = 0; i < value.getSizeX(); i++) {
				g.writeNumberField("c", value.getItem(i, j));
			}
			g.writeEndObject();
		}
		g.writeEndObject();
	}
}
