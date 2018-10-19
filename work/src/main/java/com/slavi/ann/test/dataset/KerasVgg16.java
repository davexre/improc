package com.slavi.ann.test.dataset;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.ann.test.Utils;
import com.slavi.math.matrix.Matrix;

public class KerasVgg16 {

	public void doIt(String[] args) throws Exception {
		String dir = "../python/target/vgg16/";
		List layers;
		try (InputStream fin = new FileInputStream(dir + "all_layers.json")) {
			ObjectMapper om = Utils.jsonMapper();
			layers = om.readValue(fin, ArrayList.class);
		}
		System.out.println(layers);
	}

	public void doIt2(String[] args) throws Exception {
		ObjectMapper om = Utils.xmlMapper();
		Matrix m = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 9");
		System.out.println(om.writeValueAsString(m));
	}

	public static void main(String[] args) throws Exception {
		new KerasVgg16().doIt2(args);
		System.out.println("Done.");
	}
}
