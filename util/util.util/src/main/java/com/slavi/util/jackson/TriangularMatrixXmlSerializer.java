package com.slavi.util.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.slavi.math.matrix.TriangularMatrix;

public class TriangularMatrixXmlSerializer extends StdSerializer<TriangularMatrix> {
	public TriangularMatrixXmlSerializer() {
		super(TriangularMatrix.class);
	}

	@Override
	public void serialize(TriangularMatrix value, JsonGenerator g, SerializerProvider provider)
			throws IOException {
		g.writeStartObject();
		boolean isUpper = value.isUpper();
		if (isUpper) {
			value.transpose();
		}

		if (g instanceof ToXmlGenerator) {
			((ToXmlGenerator) g).setNextIsAttribute(true);
		}
		g.writeBooleanField("upper", isUpper);
		g.writeNumberField("rows", value.getSizeY());
		g.writeNumberField("cols", value.getSizeX());
		if (g instanceof ToXmlGenerator) {
			((ToXmlGenerator) g).setNextIsAttribute(false);
		}

		for (int j = 0; j < value.getSizeY(); j++) {
			g.writeFieldName("r");
			g.writeStartObject();
			int max = Math.min(j + 1, value.getSizeX());
			for (int i = 0; i < max; i++) {
				g.writeNumberField("c", value.getItem(i, j));
			}
			g.writeEndObject();
		}
		if (isUpper)
			value.transpose();
		g.writeEndObject();
	}
}
