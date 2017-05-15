package com.slavi.example.springBoot.example2;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.slavi.example.springBoot.example2.component.Dao;

@Configuration
@ComponentScan
@EnableJpaRepositories
@EnableAutoConfiguration
@EnableTransactionManagement
public class Config {
/*
	@Bean
	public static Jackson2ObjectMapperBuilderCustomizer customizeJson() {
		return new Jackson2ObjectMapperBuilderCustomizer() {
			public void customize(Jackson2ObjectMapperBuilder r) {
				r.serializationInclusion(JsonInclude.Include.NON_EMPTY);
				AnnotationIntrospector i1 = new JacksonAnnotationIntrospector();
				AnnotationIntrospector i2 = new JaxbAnnotationIntrospector();
				AnnotationIntrospector pair = new AnnotationIntrospectorPair(i1, i2);
				r.annotationIntrospector(pair);
				r.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			}
		};
	}
	// Bean Jackson2ObjectMapperBuilderCustomizer customizeJson or
	// bean Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder
	@Bean
	public static Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
		Jackson2ObjectMapperBuilder r = new Jackson2ObjectMapperBuilder();
		r.serializationInclusion(JsonInclude.Include.NON_NULL);
		AnnotationIntrospector i1 = new JacksonAnnotationIntrospector();
		AnnotationIntrospector i2 = new JaxbAnnotationIntrospector();
		AnnotationIntrospector pair = new AnnotationIntrospectorPair(i1, i2);
		r.annotationIntrospector(pair);
		r.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return r;
	}
*/

	@Primary
	@Bean
	public static ObjectMapper jsonObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		configureObjectMapper(m);
		return m;
	}

	@Bean
	public static ObjectMapper xmlObjectMapper() {
		ObjectMapper m = new XmlMapper();
		configureObjectMapper(m);
		return m;
	}

	@Bean
	public static ObjectMapper prettyJsonObjectMapper() {
		ObjectMapper m = jsonObjectMapper();
		m.configure(SerializationFeature.INDENT_OUTPUT, true);
		m.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		return m;
	}

	private static void configureObjectMapper(ObjectMapper m) {
		AnnotationIntrospector i1 = new JacksonAnnotationIntrospector();
		AnnotationIntrospector i2 = new JaxbAnnotationIntrospector();
		AnnotationIntrospector pair = new AnnotationIntrospectorPair(i1, i2);
		m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		m.setAnnotationIntrospector(pair);
/*		Hibernate4Module hmodule = new Hibernate4Module();
		hmodule.configure(Hibernate4Module.Feature.FORCE_LAZY_LOADING, true);
		m.registerModule(hmodule);
		m.registerModule(new JaxbAnnotationModule());*/
		m.setSerializationInclusion(Include.NON_EMPTY);
	}

	@Autowired
	protected Dao dao;

	@PostConstruct
	public void initialize() throws Exception {
		dao.populateInitialData();
		dao.test();
	}
}
