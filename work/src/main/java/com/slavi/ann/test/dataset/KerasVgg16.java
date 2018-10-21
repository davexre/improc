package com.slavi.ann.test.dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.ann.test.Utils;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Util;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;

public class KerasVgg16 {

	static class LayerDesc {
		public String name;
		public List<String> weights;
	}

	static class AllLayers {
		public List<LayerDesc> layers;
	}

	public void doIt(String[] args) throws Exception {
		String dir = "../python/target/vgg16/";
		List layers;
		try (InputStream fin = new FileInputStream(dir + "all_layers.json")) {
			ObjectMapper om = Utils.jsonMapper();
			layers = om.readValue(fin, List.class);

			Matrix m = om.readValue(new FileInputStream(dir + "block4_conv3_W.json"), Matrix.class);
			System.out.println(m);
			double[] b = om.readValue(new FileInputStream(dir + "block1_conv1_b.json"), double[].class);
			System.out.println(Arrays.toString(b));
		}
		System.out.println(layers);
	}

	public void doIt2(String[] args) throws Exception {
		String fname = new File("./keras/models/vgg16_weights_tf_dim_ordering_tf_kernels.h5").getAbsolutePath();
		int file_id = H5.H5Fopen(fname, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
		//int dataset_id = H5.H5Dopen(file_id, dsname, HDF5Constants.H5P_DEFAULT);
	}

	public static void main(String[] args) throws Exception {
		new KerasVgg16().doIt(args);
		System.out.println("Done.");
	}
}
