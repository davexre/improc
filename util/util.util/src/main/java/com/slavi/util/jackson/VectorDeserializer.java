package com.slavi.util.jackson;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.slavi.math.matrix.Vector;

public class VectorDeserializer extends StdDeserializer<Vector> {
	public VectorDeserializer() {
		super(Vector.class);
	}

	@Override
	public Vector deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		ArrayList<Double> data = new ArrayList();
		while (p.nextToken().id() != JsonTokenId.ID_END_ARRAY) {
			data.add(p.getValueAsDouble());
		}
		double m[] = new double[data.size()];
		for (int i = 0; i < data.size(); i++) {
			m[i] = data.get(i);
		}
		return new Vector(m);
	}
}
