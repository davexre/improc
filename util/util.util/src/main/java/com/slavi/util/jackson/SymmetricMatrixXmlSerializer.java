package com.slavi.util.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.slavi.math.matrix.SymmetricMatrix;

public class SymmetricMatrixXmlSerializer extends StdSerializer<SymmetricMatrix> {
	public SymmetricMatrixXmlSerializer() {
		super(SymmetricMatrix.class);
	}

	@Override
	public void serialize(SymmetricMatrix value, JsonGenerator g, SerializerProvider provider)
			throws IOException {
		g.writeStartObject();
		for (int j = 0; j < value.getSizeY(); j++) {
			g.writeFieldName("r");
			g.writeStartObject();
			for (int i = 0; i <= j; i++) {
				g.writeNumberField("c", value.getItem(i, j));
			}
			g.writeEndObject();
		}
		g.writeEndObject();
	}
}
