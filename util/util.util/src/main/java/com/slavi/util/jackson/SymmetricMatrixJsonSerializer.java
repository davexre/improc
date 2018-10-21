package com.slavi.util.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.slavi.math.matrix.SymmetricMatrix;

public class SymmetricMatrixJsonSerializer extends StdSerializer<SymmetricMatrix> {
	public SymmetricMatrixJsonSerializer() {
		super(SymmetricMatrix.class);
	}

	@Override
	public void serialize(SymmetricMatrix value, JsonGenerator g, SerializerProvider provider)
			throws IOException {
		if (g instanceof ToXmlGenerator) {
			g.writeStartObject();
			for (int j = 0; j < value.getSizeM(); j++) {
				g.writeFieldName("r");
				g.writeStartObject();
				for (int i = 0; i <= j; i++) {
					g.writeNumberField("c", value.getItem(i, j));
				}
				g.writeEndObject();
			}
			g.writeEndObject();
		} else {
			g.writeStartArray();
			double r[] = new double[value.getSizeM()];
			for (int j = 0; j < value.getSizeM(); j++) {
				for (int i = 0; i <= j; i++)
					r[i] = value.getItem(i, j);
				g.writeArray(r, 0, j + 1);
			}
			g.writeEndArray();
		}
	}
}
