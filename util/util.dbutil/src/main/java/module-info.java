module util.dbutil {
	requires org.apache.commons.lang3;
	requires slf4j.api;

	requires transitive commons.dbutils;
	requires transitive java.sql;

	exports com.slavi.dbutil;
}
