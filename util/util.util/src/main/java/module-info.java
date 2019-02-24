module com.slavi.util.util {
	requires transitive com.slavi.util.math;
	requires com.fasterxml.jackson.dataformat.xml;
	requires com.fasterxml.jackson.module.jaxb;
	requires java.management;
	requires org.apache.commons.lang3;
	requires slf4j.api;

	requires transitive com.fasterxml.jackson.annotation;
	requires transitive com.fasterxml.jackson.core;
	requires transitive com.fasterxml.jackson.databind;
	requires transitive commons.math3;
	requires transitive java.desktop;

	exports com.slavi.util;
	exports com.slavi.util.concurrent;
	exports com.slavi.util.jackson;
	exports com.slavi.util.tree;
}
