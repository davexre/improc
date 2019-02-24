module com.slavi.util.dbutil {
	requires org.apache.commons.lang3;
	requires slf4j.api;

	requires transitive commons.dbutils;
	requires transitive java.sql;

	uses java.sql.Driver;

	exports com.slavi.dbutil;
	exports com.slavi.jdbcspy;

	provides java.sql.Driver with com.slavi.jdbcspy.SpyDriver;
}
