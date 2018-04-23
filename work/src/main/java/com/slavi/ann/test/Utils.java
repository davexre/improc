package com.slavi.ann.test;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class Utils {
	static final Logger log = LoggerFactory.getLogger(Utils.class);

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

	public static void downloadDataFiles(String dataTargetDir, String dataUrl, String ... dataFiles ) throws Exception {
		File dir = new File(dataTargetDir);
		dir.mkdirs();
		URL url = new URL(dataUrl);
		for (String f : dataFiles) {
			File targetFile = new File(dir, f);
			if (!targetFile.isFile()) {
				log.info("Downloading file {}", targetFile);
				FileUtils.copyURLToFile(new URL(url, f), targetFile);
			}
		}
	}
}
