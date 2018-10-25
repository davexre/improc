package com.slavi.jackson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.util.StringPrintStream;

public class TestJackson {

	public static class Filter {
		public String име;

		public void setМетодНаКирилица(String s) {

		}

		public String getМетодНаКирилица() {
			return "Верно е бе, дейба";
		}
	}



	public static void main(String[] args) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Filter f = new Filter();
		f.име = "да бе";
		String fs = mapper.writeValueAsString(f);
		System.out.println(fs);
		f = mapper.readValue(fs, Filter.class);
		System.out.println(f.име);
		System.out.println(f.getМетодНаКирилица());

		Map map = new HashMap();
		map.put(1, "asd 1");
		map.put(2, "asd 2");
		map.put(3, "asd 3");
		map.put(4, "asd 4");
		map.put(5, map.values().toArray(new String[0]));
		System.out.println(map);

		StringPrintStream out = new StringPrintStream();
		mapper.writeValue(out, map);
		System.out.println(out.toString());

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		map = mapper.readValue(in, Map.class);

		System.out.println(map);
		System.out.println(map.get("5").getClass());
	}
}
