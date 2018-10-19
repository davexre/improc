package com.slavi.jackson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.StringInputStream;
import org.reflections.Reflections;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.slavi.ann.test.Utils;
import com.slavi.ann.test.v2.Layer;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;
import com.slavi.ann.test.v2.connection.ConvolutionLayer;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.MatrixJsonModule;

public class TestCustomSerializerInJackson {

	public static class MyData {
		int a;
		String b;

		public MyData() {
		}

		public MyData(int a, String b) {
			this.a = a;
			this.b = b;
		}

		public int getA() {
			return a;
		}

		public void setA(int a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(String b) {
			this.b = b;
		}

		public String toString() {
			return "A: " + a + ", B: " + b;
		}
	}

	public static class MyDataSerializer extends StdSerializer<MyData> {
		public static final MyDataSerializer INSTANCE = new MyDataSerializer();

		private MyDataSerializer() {
			super(MyData.class);
		}

		@Override
		public void serialize(MyData value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider provider)
				throws IOException {
			gen.writeStartObject();
			gen.writeNumberField("AA", value.a);
			gen.writeStringField("BB", value.b);
			gen.writeEndObject();
		}
	}

	public static void dumpJsonParser(JsonParser p) throws IOException {
		JsonToken t = p.currentToken();
		int objCounter = 0;
		EXIT: while (t != null) {
			switch (t.id()) {
			case JsonTokenId.ID_START_OBJECT:
				objCounter++;
				System.out.println("Start object: ");
				break;
			case JsonTokenId.ID_END_OBJECT:
				objCounter--;
				System.out.println("End object: ");
				if (objCounter == 0)
					break EXIT;
				break;
			case JsonTokenId.ID_START_ARRAY:
				System.out.println("Start array: " + p.getCurrentName());
				break;
			case JsonTokenId.ID_END_ARRAY:
				System.out.println("End array: " + p.getCurrentName());
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
				System.out.println("Float: " + p.getFloatValue());
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
	}

	public static class MyJacksonModule extends SimpleModule {
		public MyJacksonModule() {
			super(new Version(1, 0, 0, null, null, null));
			addSerializer(MyData.class, MyDataSerializer.INSTANCE);
		}
	}

	public void doIt(String[] args) throws Exception {
		//MyData o1 = new MyData(123, "abc");
		//Matrix o1 = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 9");
		//ConvolutionLayer o1 = new ConvolutionLayer(3, 3, 1);
		Network o1 = new NetworkBuilder(10, 10)
			.addConvolutionLayer(3)
			.addDebugLayer("debug 1")
			.addConvolutionLayer(3)
			.addReLULayer()
			.addFullyConnectedLayer(3)
			.addSigmoidLayer()
			.build();

		ObjectMapper om = Utils.xmlMapper();
		om.registerModule(new MatrixJsonModule());
		om.registerModule(new MyJacksonModule());
		Reflections reflections = new Reflections(Layer.class.getPackage().getName());
		Map<String, NamedType> map = new HashMap<>();
		for (Class<? extends Layer> i : reflections.getSubTypesOf(Layer.class)) {
			String name = i.getSimpleName();
			if (name.endsWith("Layer"))
				name = name.substring(0, name.length() - "Layer".length());
			map.put(name, new NamedType(i, name));
		}

		om.getDeserializationConfig().getSubtypeResolver().registerSubtypes(map.values().toArray(new NamedType[map.size()]));
		String value = om.writeValueAsString(o1);
		System.out.println(value);
		Network o2 = om.readValue(new StringInputStream(value), Network.class);
		System.out.println(o2);
		System.out.println();
	}

	public static void main(String[] args) throws Exception {
		new TestCustomSerializerInJackson().doIt(args);
		System.out.println("Done.");
	}
}
