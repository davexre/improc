package com.slavi.util.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.slavi.math.matrix.DiagonalMatrix;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.SymmetricMatrix;
import com.slavi.math.matrix.TriangularMatrix;
import com.slavi.math.matrix.Vector;

/**
 * Usage:
 * com.fasterxml.jackson.databind.ObjectMapper om;
 * om.registerModule(new MatrixJsonModule);
 */
public class MatrixXmlModule extends SimpleModule {
	public MatrixXmlModule() {
		super(new Version(1, 0, 0, null, null, null));

		addSerializer(Matrix.class, new MatrixXmlSerializer());
		addDeserializer(Matrix.class, new MatrixDeserializer());

		addSerializer(SymmetricMatrix.class, new SymmetricMatrixXmlSerializer());
		addDeserializer(SymmetricMatrix.class, new SymmetricMatrixDeserializer());

		addSerializer(DiagonalMatrix.class, new DiagonalMatrixSerializer());
		addDeserializer(DiagonalMatrix.class, new DiagonalMatrixDeserializer());

		addSerializer(TriangularMatrix.class, new TriangularMatrixXmlSerializer());
		addDeserializer(TriangularMatrix.class, new TriangularMatrixDeserializer());

		addSerializer(Vector.class, new VectorSerializer());
		addDeserializer(Vector.class, new VectorDeserializer());
	}
}