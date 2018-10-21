package com.slavi.jackson;

import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.util.StringPrintStream;
import com.slavi.util.Util;

public class TestInheritanceInJackson {

	public static class BaseObject {
		@XmlAttribute
		public String title = "Base title";

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void print() {
			System.out.println(getTitle());
			System.out.println(title);
		}
	}

	public static class ExtObject extends BaseObject {
		@XmlAttribute
		public String title = "Ext title";

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}

	void doIt() throws Exception {
		ObjectMapper mapper = Util.xmlMapper(); // new ObjectMapper();

		BaseObject o = new ExtObject();
		o.print();

		Class clazz = o.getClass();
		for (Field f : clazz.getFields()) {
			System.out.println(f.getDeclaringClass().getName() + "." + f.getName() + " = " + f.get(o));
		}
		System.out.println("------------");

		StringPrintStream out = new StringPrintStream();
		mapper.writeValue(out, o);
		System.out.println(out.toString());

		o.title = "My title";
		o.print();

		out = new StringPrintStream();
		mapper.writeValue(out, o);
		System.out.println(out.toString());

		((ExtObject) o).title = "My title";
		o.print();

		out = new StringPrintStream();
		mapper.writeValue(out, o);
		System.out.println(out.toString());
	}

	public static void main(String[] args) throws Exception {
		new TestInheritanceInJackson().doIt();
		System.out.println("Done.");
	}
}
