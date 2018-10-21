package com.slavi.jackson;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.plexus.util.StringInputStream;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.util.Util;

public class TestListsInJackson {
	@JsonTypeInfo(
			use = JsonTypeInfo.Id.NAME,
			include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
			property = "type")
	@JsonSubTypes({
		@JsonSubTypes.Type(name="md1", value = MyData.class),
		@JsonSubTypes.Type(name="md2", value = MyData2.class)
	})
//	@XmlSeeAlso({ MyData.class, MyData2.class })
	public static interface MyBaseData {}

	@XmlRootElement
	public static class MyData implements MyBaseData {
		public int a;
		public String b;

		public MyData() {}

		public MyData(int a, String b) {
			this.a = a;
			this.b = b;
		}
	}

	@XmlRootElement
	public static class MyData2 implements MyBaseData {
		public int a2;
		public String b2;

		public MyData2() {}

		public MyData2(int a2, String b2) {
			this.a2 = a2;
			this.b2 = b2;
		}
	}

	public static class MyDataList {
//		@XmlElementWrapper(name="items")
		@XmlElement(name="item")
		public List<MyBaseData> dataItems = new ArrayList<>();
	}

	void doIt() throws Exception {
//		ObjectMapper om = Util.jsonMapper();
		ObjectMapper om = Util.xmlMapper();
		MyDataList d = new MyDataList();
		d.dataItems.add(new MyData(123, "asd"));
		d.dataItems.add(new MyData2(234, "qwe"));
		String value = om.writeValueAsString(d);
		System.out.println(value);
		MyDataList d2 = om.readValue(new StringInputStream(value), MyDataList.class);
	}

	public static void main(String[] args) throws Exception {
		new TestListsInJackson().doIt();
	}
}
