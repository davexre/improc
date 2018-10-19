package com.slavi.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.slavi.math.matrix.Matrix;

public class MatrixJsonSerializer extends StdSerializer<Matrix> {
	public MatrixJsonSerializer() {
		super(Matrix.class);
	}

	@Override
	public void serialize(Matrix value, com.fasterxml.jackson.core.JsonGenerator g, SerializerProvider provider)
			throws IOException {
		if (g instanceof ToXmlGenerator) {
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
		} else {
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
}
