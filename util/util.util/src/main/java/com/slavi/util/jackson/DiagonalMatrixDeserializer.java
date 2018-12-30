package com.slavi.util.jackson;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.slavi.math.matrix.DiagonalMatrix;

public class DiagonalMatrixDeserializer extends StdDeserializer<DiagonalMatrix> {
	public DiagonalMatrixDeserializer() {
		super(DiagonalMatrix.class);
	}

	@Override
	public DiagonalMatrix deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		if (p.currentToken().id() != JsonTokenId.ID_START_OBJECT)
			throw new JsonParseException(p, "Unexpected token");
		int rows = 0;
		int cols = 0;
		ArrayList<Double> data = new ArrayList();

		while (true) {
			JsonToken t = p.nextToken();
			if (t.id() == JsonTokenId.ID_END_OBJECT)
				break;
			if (t.id() == JsonTokenId.ID_FIELD_NAME) {
				if ("rows".equals(p.getCurrentName())) {
					p.nextToken();
					rows = p.getValueAsInt();
				} else if ("cols".equals(p.getCurrentName())) {
					p.nextToken();
					cols = p.getValueAsInt(0);
				} else if ("r".equals(p.getCurrentName())) {
					p.nextToken();
					if (p.currentTokenId() == JsonTokenId.ID_START_ARRAY) {
						JsonToken tt = p.nextToken();
						while (tt.id() != JsonTokenId.ID_END_ARRAY) {
							data.add(p.getValueAsDouble());
							tt = p.nextToken();
						}
					} else {
						data.add(p.getValueAsDouble());
					}
				}
			} else {
				throw new JsonParseException(p, "Unexpected token");
			}
		}

		if (rows == 0 && cols == 0) {
			rows = cols = data.size();
		}

		DiagonalMatrix r = new DiagonalMatrix(cols, rows);
		int maxIndex = Math.min(data.size(), Math.min(cols, rows));
		for (int i = 0; i < maxIndex; i++) {
			r.setItem(i, i, data.get(i));
		}
		return r;
	}
}
