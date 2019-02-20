module com.slavi.util.io {
	requires jdk.unsupported;
	requires org.apache.commons.lang3;

	requires transitive java.desktop;
	requires transitive jdom;
	requires transitive org.apache.commons.io;

	exports com.slavi.util.file;
	exports com.slavi.util.io;
	exports com.slavi.util.xml;
}