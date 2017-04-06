package com.slavi.ann.test;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class Utils {
	public static void configureMapper(ObjectMapper m) {
		AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
		AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
		AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);

		m.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		//m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
		m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		m.setAnnotationIntrospector(pair);
		m.enable(SerializationFeature.INDENT_OUTPUT);
	}

	public static ObjectMapper xmlMapper() {
		ObjectMapper m = new XmlMapper();
		configureMapper(m);
		return m;
	}

	public static ObjectMapper jsonMapper() {
		ObjectMapper m = new ObjectMapper();
		configureMapper(m);
		return m;
	}

}
