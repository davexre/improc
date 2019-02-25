module com.slavi.util.util {
	requires transitive com.slavi.util.math;

	requires transitive com.fasterxml.jackson.annotation;
	requires transitive com.fasterxml.jackson.core;
	requires transitive com.fasterxml.jackson.databind;
	requires transitive com.fasterxml.jackson.dataformat.xml;
	requires transitive com.fasterxml.jackson.module.jaxb;
	requires transitive java.desktop;
	requires transitive java.management;
	requires transitive org.apache.commons.lang3;
	requires transitive slf4j.api;
	requires transitive commons.math3;

	exports com.slavi.util;
	exports com.slavi.util.concurrent;
	exports com.slavi.util.jackson;
	exports com.slavi.util.tree;
}
