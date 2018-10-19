package com.slavi.util;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.slavi.math.matrix.Matrix;

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
	}
}