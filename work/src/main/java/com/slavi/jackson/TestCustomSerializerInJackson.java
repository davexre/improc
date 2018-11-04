package com.slavi.jackson;

import java.io.IOException;

import org.codehaus.plexus.util.StringInputStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.slavi.ann.test.Utils;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;

public class TestCustomSerializerInJackson {

	public static class MyEnumJsonConverter {
		final static String[] ITEMS = {"bit1", "bit2", "bit3"};

		public static class Serialize extends BitJsonConverterBase.Serialize {
			public Serialize() {
				super(ITEMS, true);
			}
		}

		public static class Deserialize extends BitJsonConverterBase.Deserialize {
			public Deserialize() {
				super(ITEMS, true, true);
			}
		}
	}

	public static class MyData {
		@JsonSerialize(converter=MyEnumJsonConverter.Serialize.class)
		@JsonDeserialize(converter=MyEnumJsonConverter.Deserialize.class)
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
//			addSerializer(MyData.class, MyDataSerializer.INSTANCE);
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

		ObjectMapper om = Utils.jsonMapper();
		om.registerModule(new MyJacksonModule());

		String value = om.writeValueAsString(o1);
		System.out.println(value);
		Network o2 = om.readValue(new StringInputStream(value), Network.class);
		//MyData o2 = om.readValue(new StringInputStream(value), MyData.class);
		System.out.println(o2);
		System.out.println();
	}

	public static void main(String[] args) throws Exception {
		new TestCustomSerializerInJackson().doIt(args);
		System.out.println("Done.");
	}
}
