package com.slavi.util.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.SymmetricMatrix;

/**
 * Usage:
 * com.fasterxml.jackson.databind.ObjectMapper om;
 * om.registerModule(new MatrixJsonModule);
 */
public class MatrixJsonModule extends SimpleModule {
	public MatrixJsonModule() {
		super(new Version(1, 0, 0, null, null, null));

		addSerializer(Matrix.class, new MatrixJsonSerializer());
		addDeserializer(Matrix.class, new MatrixJsonDeserializer());

		addSerializer(SymmetricMatrix.class, new SymmetricMatrixJsonSerializer());
		addDeserializer(SymmetricMatrix.class, new SymmetricMatrixJsonDeserializer());
	}
}