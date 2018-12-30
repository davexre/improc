package com.slavi.util.jackson;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.slavi.math.matrix.TriangularMatrix;

public class TriangularMatrixDeserializer extends StdDeserializer<TriangularMatrix> {
	public TriangularMatrixDeserializer() {
		super(TriangularMatrix.class);
	}

	ArrayList<Double> readArray(JsonParser p) throws IOException {
		ArrayList<Double> row = new ArrayList<>();
		while (true) {
			switch (p.currentTokenId()) {
			case JsonTokenId.ID_END_OBJECT:
			case JsonTokenId.ID_END_ARRAY:
				p.nextToken();
				return row;
			case JsonTokenId.ID_FIELD_NAME:
				if (!"c".equals(p.getCurrentName()))
					throw new JsonParseException(p, "Unexpected token");
				break;
			default:
				row.add(p.getValueAsDouble());
			}
			p.nextToken();
		}
	}

	@Override
	public TriangularMatrix deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		boolean isUpper = false;
		int sizeX = 0;
		int sizeY = 0;
		ArrayList<ArrayList<Double>> rows = new ArrayList<>();

		p.nextToken(); // JsonTokenId.ID_START_OBJECT
		while (true) {
			if (p.currentTokenId() == JsonTokenId.ID_END_OBJECT) {
				break;
			} else if (p.currentTokenId() == JsonTokenId.ID_FIELD_NAME) {
				if ("upper".equals(p.getCurrentName())) {
					p.nextToken();
					isUpper = p.getValueAsBoolean();
				} else if ("rows".equals(p.getCurrentName())) {
					p.nextToken();
					sizeY = p.getValueAsInt();
				} else if ("cols".equals(p.getCurrentName())) {
					p.nextToken();
					sizeX = p.getValueAsInt();
				} else if ("r".equals(p.getCurrentName())) {
					p.nextToken();
					if (p.currentTokenId() == JsonTokenId.ID_START_ARRAY) {
						p.nextToken();
						if (p.currentTokenId() == JsonTokenId.ID_START_ARRAY) {
							while (p.currentTokenId() == JsonTokenId.ID_START_ARRAY) {
								p.nextToken();
								rows.add(readArray(p));
							}
						} else {
							rows.add(readArray(p));
							continue;
						}
					} else if (p.currentTokenId() == JsonTokenId.ID_START_OBJECT) {
						p.nextToken();
						rows.add(readArray(p));
						continue;
					} else {
						throw new JsonParseException(p, "Unexpected token");
					}
				} else {
					throw new JsonParseException(p, "Unexpected token");
				}
			} else {
				throw new JsonParseException(p, "Unexpected token");
			}
			p.nextToken();
		}

		if (sizeY == 0)
			sizeY = rows.size();

		if (sizeX == 0) {
			for (int j = 0; j < rows.size(); j++) {
				ArrayList<Double> row = rows.get(j);
				if (sizeX < row.size())
					sizeX = row.size();
			}
		}

		TriangularMatrix m = new TriangularMatrix(sizeX, sizeY, false);
		for (int j = 0; j < rows.size(); j++) {
			ArrayList<Double> row = rows.get(j);
			int maxIndex = Math.min(row.size() - 1, Math.min(sizeX, j));
			for (int i = 0; i <= maxIndex; i++) {
				m.setItem(i, j, row.get(i));
			}
		}
		if (isUpper)
			m.transpose();
		return m;
	}
}
