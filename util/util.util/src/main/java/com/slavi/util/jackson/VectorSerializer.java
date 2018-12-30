package com.slavi.util.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.slavi.math.matrix.Vector;

public class VectorSerializer extends StdSerializer<Vector> {
	public VectorSerializer() {
		super(Vector.class);
	}

	@Override
	public void serialize(Vector value, JsonGenerator g, SerializerProvider provider)
			throws IOException {
		double r[] = value.getVector();
		g.writeArray(r, 0, r.length);
	}
}
