package com.slavi.util.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.slavi.math.matrix.DiagonalMatrix;

public class DiagonalMatrixSerializer extends StdSerializer<DiagonalMatrix> {
	public DiagonalMatrixSerializer() {
		super(DiagonalMatrix.class);
	}

	@Override
	public void serialize(DiagonalMatrix value, JsonGenerator g, SerializerProvider provider)
			throws IOException {
		double r[] = value.getVector();
		g.writeStartObject();

		if (g instanceof ToXmlGenerator) {
			((ToXmlGenerator) g).setNextIsAttribute(true);
		}
		g.writeNumberField("rows", value.getSizeY());
		g.writeNumberField("cols", value.getSizeX());
		if (g instanceof ToXmlGenerator) {
			((ToXmlGenerator) g).setNextIsAttribute(false);
		}

		g.writeFieldName("r");
		g.writeArray(r, 0, r.length);
		g.writeEndObject();
	}
}
