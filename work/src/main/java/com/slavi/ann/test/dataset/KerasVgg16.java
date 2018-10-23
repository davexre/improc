package com.slavi.ann.test.dataset;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.slavi.ann.test.Utils;

public class KerasVgg16 {

	static class LayerDesc {
		public String name;
		public int numCount;
		public List<String> weights;
	}

	static class AllLayers {
		public List<LayerDesc> layers;
	}

	public static class DummyJsonDeserializer extends StdDeserializer<LayerDesc> {
		public DummyJsonDeserializer() {
			super(LayerDesc.class);
		}

		@Override
		public LayerDesc deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {

			int objCounter = 0;
			int arrCounter = 0;
			int numCount = 0;

			JsonToken t = p.currentToken();
			EXIT: while (t != null) {
				switch (t.id()) {
				case JsonTokenId.ID_START_OBJECT:
					objCounter++;
					break;
				case JsonTokenId.ID_END_OBJECT:
					objCounter--;
					if (objCounter == 0 && arrCounter == 0)
						break EXIT;
					break;
				case JsonTokenId.ID_START_ARRAY:
					arrCounter++;
					break;
				case JsonTokenId.ID_END_ARRAY:
					arrCounter--;
					if (objCounter == 0 && arrCounter == 0)
						break EXIT;
					break;
				case JsonTokenId.ID_FIELD_NAME:
					System.out.println("Field name: " + p.getCurrentName());
					break;
				case JsonTokenId.ID_STRING:
					System.out.println("String: " + p.getText());
					break;
				case JsonTokenId.ID_NUMBER_INT:
					System.out.println("Int: " + p.getIntValue());
					break;
				case JsonTokenId.ID_NUMBER_FLOAT:
					numCount++;
					break;
				case JsonTokenId.ID_TRUE:
					System.out.println("True: " + p.getCurrentName());
					break;
				case JsonTokenId.ID_FALSE:
					System.out.println("False: " + p.getCurrentName());
					break;
				case JsonTokenId.ID_NULL:
					System.out.println("Null: " + p.getCurrentName());
					break;
				case JsonTokenId.ID_EMBEDDED_OBJECT:
					System.out.println("Embedded object: " + p.getCurrentName());
					break;
				case JsonTokenId.ID_NO_TOKEN:
					break EXIT;
				}
				t = p.nextToken();
			}

			LayerDesc r = new LayerDesc();
			r.numCount = numCount;
			return r;
		}
	}

	public static class DummyJacksonModule extends SimpleModule {
		public DummyJacksonModule() {
			super(new Version(1, 0, 0, null, null, null));
			addDeserializer(LayerDesc.class, new DummyJsonDeserializer());
		}
	}


	public void doIt(String[] args) throws Exception {
		String dir = "../python/target/vgg16/";
		List layers;
		try (InputStream fin = new FileInputStream(dir + "all_layers.json")) {
			ObjectMapper om = Utils.jsonMapper();
			om.registerModule(new DummyJacksonModule());
			layers = om.readValue(fin, List.class);
			Map<String, Integer> map = new HashedMap<>();
			int total = 0;
			for (int i = 0; i < layers.size(); i++) {
				List l = (List) layers.get(i);
				String name = (String) l.get(0);
				l = (List) l.get(1);
				for (int j = 0; j < l.size(); j++) {
					String w = (String) l.get(j);
					try (InputStream fw = new FileInputStream(dir + w + ".json")) {
						LayerDesc ld = om.readValue(fw, LayerDesc.class);
						map.put(w, ld.numCount);
						total += ld.numCount;
					}
				}
			}
			System.out.println(map);
			System.out.println(total);
/*
			Matrix m = om.readValue(new FileInputStream(dir + "block4_conv3_W.json"), Matrix.class);
			System.out.println(m);
			double[] b = om.readValue(new FileInputStream(dir + "block1_conv1_b.json"), double[].class);
			System.out.println(Arrays.toString(b));*/
		}
		System.out.println(layers);
	}

	public void doIt2(String[] args) throws Exception {
		//String fname = new File("./keras/models/vgg16_weights_tf_dim_ordering_tf_kernels.h5").getAbsolutePath();
		//int file_id = H5.H5Fopen(fname, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
		//int dataset_id = H5.H5Dopen(file_id, dsname, HDF5Constants.H5P_DEFAULT);
	}

	public static void main(String[] args) throws Exception {
		new KerasVgg16().doIt(args);
		System.out.println("Done.");
	}
}
