package com.slavi.jackson;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.util.Util;

public class TestJacksonInject {

	static class Injectable {
		String name;

		public Injectable(String name) {
			this.name = name;
		}

		public String toString() {
			return "name=" + name;
		}
	}

	public static class Bean {
		private final Injectable inj;
		public String data;

		public Bean(@JacksonInject(value="asd") Injectable inj) {
			this.inj = inj;
		}

		public String toString() {
			return "data=" + data + ", inj=(" + inj + ")";
		}
	}

	public static void main(String[] args) throws Exception {
		ObjectMapper m = Util.jsonMapper();
		Map map = new HashMap();
		map.put("asd", new Injectable("INJECTED"));
		m.setInjectableValues(new InjectableValues.Std(map));
		Bean b = m.readValue("{ data:'qwe' }", Bean.class);
		System.out.println(b);
	}
}
