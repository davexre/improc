module com.slavi.util.log {
	requires transitive logback.classic;
	requires transitive logback.core;
	requires transitive slf4j.api;

	exports com.slavi.log;

	provides ch.qos.logback.classic.spi.Configurator with com.slavi.log.DefaultSlf4jConfigurator;
}
