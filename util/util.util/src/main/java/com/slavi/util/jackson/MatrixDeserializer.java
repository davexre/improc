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
import com.slavi.math.matrix.Matrix;

public class MatrixDeserializer extends StdDeserializer<Matrix> {
	public MatrixDeserializer() {
		super(Matrix.class);
	}

	@Override
	public Matrix deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		ArrayList<ArrayList<Double>> rows = new ArrayList<>();
		int maxCols = 0;
		JsonToken t = p.currentToken();
		switch (t.id()) {
		case JsonTokenId.ID_START_OBJECT:
			while (true) {
				JsonToken tt = p.nextToken();
				if (tt.id() == JsonTokenId.ID_END_OBJECT)
					break;
				if (tt.id() != JsonTokenId.ID_FIELD_NAME ||
					!"r".equals(p.getCurrentName()))
					throw new JsonParseException(p, "Unexpected token");
				tt = p.nextToken();
				if (tt.id() != JsonTokenId.ID_START_OBJECT)
					throw new JsonParseException(p, "Unexpected token");

				ArrayList<Double> row = new ArrayList<>();
				while (true) {
					tt = p.nextToken();
					if (tt.id() == JsonTokenId.ID_END_OBJECT)
						break;
					if (tt.id() != JsonTokenId.ID_FIELD_NAME ||
						!"c".equals(p.getCurrentName()))
						throw new JsonParseException(p, "Unexpected token");
					tt = p.nextToken();
					row.add(p.getValueAsDouble());
				}
				rows.add(row);
				if (maxCols < row.size())
					maxCols = row.size();
			}
			break;

		case JsonTokenId.ID_START_ARRAY:
			while (true) {
				JsonToken tt = p.nextToken();
				if (tt.id() == JsonTokenId.ID_END_ARRAY)
					break;
				if (tt.id() != JsonTokenId.ID_START_ARRAY)
					throw new JsonParseException(p, "Unexpected token");

				ArrayList<Double> row = new ArrayList<>();
				while (true) {
					tt = p.nextToken();
					if (tt.id() == JsonTokenId.ID_END_ARRAY)
						break;
					row.add(p.getValueAsDouble());
				}
				rows.add(row);
				if (maxCols < row.size())
					maxCols = row.size();
			}
			break;
		default:
			throw new JsonParseException(p, "Unexpected token");
		}

		Matrix m = new Matrix(maxCols, rows.size());
		for (int j = 0; j < rows.size(); j++) {
			ArrayList<Double> row = rows.get(j);
			for (int i = 0; i < row.size(); i++) {
				m.setItem(i, j, row.get(i));
			}
		}
		return m;
	}
}
