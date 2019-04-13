package com.slavi.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.slavi.util.jackson.MatrixJsonModule;
import com.slavi.util.jackson.MatrixXmlModule;
import com.slavi.util.jackson.PostLoad;

/**
 * This class contains utility static methods for general purpose.
 */
public class Util {
	public final static String hexChars = "0123456789ABCDEF";

	// https://stackoverflow.com/a/9855338/2243209
	public static String bytesToHex(byte[] bytes) {
		char[] r = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			r[j * 2] = hexChars.charAt(v >>> 4);
			r[j * 2 + 1] = hexChars.charAt(v & 0x0F);
		}
		return new String(r);
	}

	public static byte[] hexToBytes(String hexStr) throws NumberFormatException {
		if (hexStr.length() % 2 != 0)
			throw new NumberFormatException("Number of chars in hex string must be multiple of 2 but was " + hexStr.length());
		byte[] r = new byte[hexStr.length() / 2];
		for (int i = 0, ii = -1; i < r.length; i++) {
			int a1 = hexChars.indexOf(Character.toUpperCase(hexStr.charAt(++ii)));
			if (a1 < 0) throw new NumberFormatException("Invalid char " + hexStr.charAt(ii) + " at position " + ii);
			int a2 = hexChars.indexOf(Character.toUpperCase(hexStr.charAt(++ii)));
			if (a2 < 0) throw new NumberFormatException("Invalid char " + hexStr.charAt(ii) + " at position " + ii);
			r[i] = (byte) (((a1 << 4) + a2) & 0xff);
		}
		return r;
	}

	/**
	 * Returns the "human" representation of a time delta specified in milliseconds
	 * like "1 hour 23 minutes 45.6 seconds".
	 * @param millis	the time delta in milliseconds
	 */
	public static String getFormatedMilliseconds(long millis) {
		final long[] divisors = { (1000 * 60 * 60 * 24), (1000 * 60 * 60),
				(1000 * 60), (1000) };
		final String[][] texts = { { " day ", " days " },
				{ " hour ", " hours " }, { " minute ", " minutes " },
				{ " second", " seconds" } };
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			long tmp = millis / divisors[i];
			millis %= divisors[i];
			if (tmp > 0) {
				s.append(Long.toString(tmp));
				s.append(texts[i][tmp == 1 ? 0 : 1]);
			}
		}
		s.append(String.format(Locale.US, "%1$.3f", new Object[] { new Double((double) (millis) / divisors[3]) } ));
		s.append(texts[3][1]);
		return s.toString();
	}

	/**
	 * Returns the "human" representation of a size in bytes
	 * like "123 bytes", "1.2 K", "2.3 M" or "3.4 G".
	 * @param sizeInBytes	the size in bytes.
	 */
	public static String getFormatBytes(long sizeInBytes) {
		String dim = "bytes";
		double size = sizeInBytes;
		if (Math.abs(size) >= 1024.0) {
			dim = "K";
			size /= 1024.0;
		}
		if (Math.abs(size) >= 800.0) {
			dim = "M";
			size /= 1024.0;
		}
		if (Math.abs(size) >= 800.0) {
			dim = "G";
			size /= 1024.0;
		}
		if (Math.abs(size) >= 800.0) {
			dim = "T";
			size /= 1024.0;
		}
		if (Math.abs(size) >= 800.0) {
			dim = "P";
			size /= 1024.0;
		}
		if (Math.abs(size) >= 800.0) {
			dim = "E";
			size /= 1024.0;
		}
		if (Math.floor(size) == size)
			return String.format(Locale.US, "%d %s", new Object[] { new Integer((int)size), dim } );
		return String.format(Locale.US, "%.1f %s", new Object[] { new Double(size), dim } );
	}

	public static String getFormatBytes2(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		// String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		String pre = "KMGTPE".charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	static class UnitName {
		public final String name;
		public final long scale;

		public UnitName(String name, long scale) {
			this.name = name;
			this.scale = scale;
		}

		final static List<UnitName> byteSizeUnits = new ArrayList<>();
		static {
			long scale = 1;
			long scaleSI = 1;

			byteSizeUnits.add(new UnitName("B", scale));

			scale *= 1024;
			scaleSI *= 1000;
			byteSizeUnits.add(new UnitName("K", scale));
			byteSizeUnits.add(new UnitName("KB", scale));
			byteSizeUnits.add(new UnitName("KIB", scaleSI));

			scale *= 1024;
			scaleSI *= 1000;
			byteSizeUnits.add(new UnitName("M", scale));
			byteSizeUnits.add(new UnitName("MB", scale));
			byteSizeUnits.add(new UnitName("MIB", scaleSI));

			scale *= 1024;
			scaleSI *= 1000;
			byteSizeUnits.add(new UnitName("G", scale));
			byteSizeUnits.add(new UnitName("GB", scale));
			byteSizeUnits.add(new UnitName("GIB", scaleSI));

			scale *= 1024;
			scaleSI *= 1000;
			byteSizeUnits.add(new UnitName("T", scale));
			byteSizeUnits.add(new UnitName("TB", scale));
			byteSizeUnits.add(new UnitName("TIB", scaleSI));

			scale *= 1024;
			scaleSI *= 1000;
			byteSizeUnits.add(new UnitName("P", scale));
			byteSizeUnits.add(new UnitName("PB", scale));
			byteSizeUnits.add(new UnitName("PIB", scaleSI));

			scale *= 1024;
			scaleSI *= 1000;
			byteSizeUnits.add(new UnitName("E", scale));
			byteSizeUnits.add(new UnitName("EB", scale));
			byteSizeUnits.add(new UnitName("EIB", scaleSI));
		}
	}

	public static long parseFormattedBytes(String s) {
		s = s.toUpperCase().trim();
		long scale = 1;
		for (int i = UnitName.byteSizeUnits.size() - 1; i >= 0; i--) {
			UnitName un = UnitName.byteSizeUnits.get(i);
			if (s.endsWith(un.name)) {
				scale = un.scale;
				s = s.substring(0, s.length() - un.name.length()).trim();
				break;
			}
		}
		return (long) (Double.parseDouble(s) * scale);
	}

	/**
	 * Sets global proxy settings. The string has the following format:
	 * <code>
	 * [proxy type]:[proxy address]:[port]:[usename]:[password]:[no proxy address list]
	 * </code>
	 * Where:<br>
	 * [proxy type] is one of the following HTTP or SOCKS<br>
	 * [proxy address] is the name or ip of the proxy<br>
	 * [port] is the proxy port. default is 80<br>
	 * [username] is the proxy username. if empty - no user required for proxy<br>
	 * [password] is the proxy password. if empty - no password required for proxy<br>
	 * [no proxy address list] hosts which should be connected too directly and
	 * 		not through the proxy server. The value can be a list of hosts, each
	 * 		seperated by a |, and in addition a wildcard character (*) can be
	 * 		used for matching. For example: "*.foo.com|localhost"<br>
	 * <code>
	 * Examples:
	 * "HTTP:proxy:80:ivan:ivan123:*.foo.com|localhost"
	 * "HTTP:proxy:80:::localhost"
	 * "SOCKS:proxy5:1080:ivan:ivan123:*.foo.com|localhost"
	 * "SOCKS:proxy4:1080:::localhost"
	 * "" -> no proxy/clear proxy settings
	 * </code>
	 */
	public static void setProxy(String proxySettings) {
		String settings[] = proxySettings == null ? null : proxySettings.split(":");
		int count = settings == null ? 0 : settings.length;
		String type = 0 < count ? settings[0] : "";
		String host = 1 < count ? settings[1] : "";
		String port = 2 < count ? settings[2] : "";
		String user = 3 < count ? settings[3] : "";
		String pass = 4 < count ? settings[4] : "";
		String noProxyList = 5 < count ? settings[5] : "";

		Properties props = System.getProperties();

		props.remove("http.proxyHost");
		props.remove("http.proxyPort");
		props.remove("http.proxyUser");
		props.remove("http.proxyPassword");
		props.remove("http.http.nonProxyHosts");

		props.remove("https.proxyHost");
		props.remove("https.proxyPort");
		props.remove("https.http.nonProxyHosts");

		props.remove("ftp.proxyHost");
		props.remove("ftp.proxyPort");
		props.remove("ftp.nonProxyHosts");

		props.remove("socksProxyHost");
		props.remove("socksProxyPort");
		props.remove("java.net.socks.username");
		props.remove("java.net.socks.password");

		if ("HTTP".equalsIgnoreCase(type)) {
			if (!"".equals(host)) {
				props.put("http.proxyHost", host);
				props.put("https.proxyHost", host);
				props.put("ftp.proxyHost", host);
			}
			if (!"".equals(port)) {
				props.put("http.proxyPort", port);
				props.put("https.proxyPort", port);
				props.put("ftp.proxyPort", port);
			}
			if (!"".equals(user)) {
				props.put("http.proxyUser", user);
			}
			if (!"".equals(pass)) {
				props.put("http.proxyPassword", pass);
			}
			if (!"".equals(noProxyList)) {
				props.put("http.nonProxyHosts", noProxyList);
				props.put("https.nonProxyHosts", noProxyList);
				props.put("ftp.nonProxyHosts", noProxyList);
			}
		} else if ("SOCKS".equalsIgnoreCase(type)) {
			if (!"".equals(host)) {
				props.put("socksProxyHost", host);
			}
			if (!"".equals(port)) {
				props.put("socksProxyPort", port);
			}
			if (!"".equals(user)) {
				props.put("java.net.socks.username", user);
			}
			if (!"".equals(pass)) {
				props.put("java.net.socks.password", pass);
			}
		}
	}

	private static class BlockingQueuePut implements RejectedExecutionHandler {
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			try {
				executor.getQueue().put(r);
			} catch (InterruptedException ie) {
				throw new RejectedExecutionException(ie);
			}
		}
	}

	private static class WorkerThreadFactory implements ThreadFactory {
		static final AtomicInteger threadCounter = new AtomicInteger(0);

		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("Worker thread " + threadCounter.incrementAndGet());
			thread.setPriority(Thread.MIN_PRIORITY);
			return thread;
		}
	}

	/**
	 * Creates a Blocking thread pool with fixed size.
	 * @see java.util.concurrent.Executors.newFixedThreadPool
	 */
	public static ExecutorService newBlockingThreadPoolExecutor() {
		Runtime runtime = Runtime.getRuntime();
		int nThreads = runtime.availableProcessors();
		if (nThreads < 2)
			nThreads = 2;
		return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new ArrayBlockingQueue<Runnable>(1),
                                      new WorkerThreadFactory(),
                                      new BlockingQueuePut());
	}

	/**
	 * Creates a Blocking thread pool with fixed size.
	 * @see java.util.concurrent.Executors.newFixedThreadPool
	 */
	public static ExecutorService newBlockingThreadPoolExecutor(int nThreads) {
		return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new ArrayBlockingQueue<Runnable>(1),
                                      new WorkerThreadFactory(),
                                      new BlockingQueuePut());
	}

	/**
	 * Creates a Blocking thread pool with fixed size.
	 * @see java.util.concurrent.Executors.newFixedThreadPool
	 */
	public static ExecutorService newBlockingThreadPoolExecutor(int nThreads, ThreadFactory threadFactory) {
		return new ThreadPoolExecutor(nThreads, nThreads,
				0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(1),
				threadFactory,
				new BlockingQueuePut());
	}

	/**
	 * Trims the specified string or returns an empty string if value is null.
	 */
	public static String trimNZ(String value) {
		return value == null ? "" : value.trim();
	}

	/**
	 * This returns a new string with all surrounding whitespace removed and
	 * internal whitespace normalized to a single space. If only whitespace
	 * exists, the empty string is returned.
	 * <p>Per XML 1.0 Production 3 whitespace includes: #x20, #x9, #xD, #xA</p>
	 *
	 * @param str string to be normalized.
	 * @return normalized string or empty string
	 *
	 * @see org.jdom.Text#normalizeString
	 */
	public static String normalizeString(String str) {
		if (str == null)
			return "";

		char[] c = str.toCharArray();
		char[] n = new char[c.length];
		boolean white = true;
		int pos = 0;
		for (int i = 0; i < c.length; i++) {
			if (" \t\n\r".indexOf(c[i]) != -1) {
				if (!white) {
					n[pos++] = ' ';
					white = true;
				}
			} else {
				n[pos++] = c[i];
				white = false;
			}
		}
		if (white && pos > 0) {
			pos--;
		}
		return new String(n, 0, pos);
	}

	public static String arrayToString(double array[]) {
		if (array == null)
			return "";
		StringBuilder sb = new StringBuilder();
		for (double i : array) {
			sb.append(i);
			sb.append('\t');
		}
		return sb.toString();
	}

	public static String arrayToString(int array[]) {
		if (array == null)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i : array) {
			sb.append(i);
			sb.append('\t');
		}
		return sb.toString();
	}

	public static double[] stringToDoubleArray(String str) {
		StringTokenizer st = new StringTokenizer(str, "\t");
		int size = st.countTokens();
		double result[] = new double[size];
		for (int i = 0; i < size; i++) {
			String s = st.nextToken();
			double v = Double.parseDouble(s);
			result[i] = v;
		}
		return result;
	}

	public static int[] stringToIntArray(String str) {
		StringTokenizer st = new StringTokenizer(str, "\t");
		int size = st.countTokens();
		int result[] = new int[size];
		for (int i = 0; i < size; i++) {
			String s = st.nextToken();
			int v = Integer.parseInt(s);
			result[i] = v;
		}
		return result;
	}

	public static String exceptionToString(Throwable t) {
		if (t == null) return "NULL";
		StringWriter result = new StringWriter();
		PrintWriter out = new PrintWriter(result);
		t.printStackTrace(out);
		out.close();
		return result.toString();
	}

	/**
	 * Treat an {@link Enumeration} as an {@link Iterable} so it can be used in an enhanced for-loop.
	 * Bear in mind that the enumeration is "consumed" by the loop and so should be used only once.
	 * Generally it is best to put the code which obtains the enumeration inside the loop header.
	 * <div class="nonnormative">
	 * <p>Example of correct usage:</p>
	 * <pre>
	 * ClassLoader loader = ...;
	 * String name = ...;
	 * for (URL resource : NbCollections.iterable(loader.{@link ClassLoader#getResources getResources}(name))) {
	 *     // ...
	 * }
	 * </pre>
	 * </div>
	 * @param enumeration an enumeration
	 * @return an iterable wrapper which will traverse the enumeration once
	 *         ({@link Iterator#remove} is not supported)
	 * @throws NullPointerException if the enumeration is null
	 * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6349852">Java bug #6349852</a>
	 * @since org.openide.util 7.5
	 *
	 * Code borrowed from http://www.java2s.com/Code/Java/Collections-Data-Structure/TreatanEnumerationasanIterable.htm
	 */
	public static <E> Iterable<E> iterable(final Enumeration<E> enumeration) {
		if (enumeration == null) {
			throw new NullPointerException();
		}
		return new Iterable<E>() {
			public Iterator<E> iterator() {
				return new Iterator<E>() {
					public boolean hasNext() {
						return enumeration.hasMoreElements();
					}

					public E next() {
						return enumeration.nextElement();
					}

					public void remove() {
						throw new UnsupportedOperationException("Not applicable");
					}
				};
			}
		};
	}

	/**
	 * Code borrowed from:
	 * http://www.javaworld.com/javaworld/javatips/jw-javatip76.html?page=2
	 */
	public static <SerializableObject extends Serializable> SerializableObject deepCopy(SerializableObject oldObj) throws Exception {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		oos = new ObjectOutputStream(bos);
		// serialize and pass the object
		oos.writeObject(oldObj);
		oos.flush();
		ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
		ois = new ObjectInputStream(bin);
		// return the new object
		return (SerializableObject) ois.readObject();
	}

	public static <T> int indexOf(T[] objects, T object) {
		if (objects == null)
			return -1;
		for (int i = 0; i < objects.length; i++) {
			if ((objects[i] != null && objects[i].equals(object)) ||
				(object == null && objects[i] == null))
				return i;
		}
		return -1;
	}

	public static String objectToString(Object o) {
		return ReflectionToStringBuilder.toString(o, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public static <T> T nvl(T value, T defaultValue) {
		return value == null ? defaultValue : value;
	}

	public static <T> T nvl2(Object value, T valueIfNotNull, T valueIfNull) {
		return value == null ? valueIfNull : valueIfNotNull;
	}

	static Method findPostLoad(Class clazz) {
		while (clazz != null) {
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.getParameterCount() == 0 && m.getAnnotation(PostLoad.class) != null) {
					return m;
				}
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	static class MyConverter implements Converter {
		JavaType type;
		public MyConverter(JavaType type) {
			this.type = type;
		}

		@Override
		public Object convert(Object value) {
			if (value != null) {
				Method m = findPostLoad(value.getClass());
				try {
					m.invoke(value);
				} catch (Exception e) {
					throw new Error(e);
				}
			}
			return value;
		}

		@Override
		public JavaType getInputType(TypeFactory typeFactory) {
			return type;
		}

		@Override
		public JavaType getOutputType(TypeFactory typeFactory) {
			return type;
		}
	}

	static class MyAnnotationIntrospector extends AnnotationIntrospector {
		@Override
		public Version version() {
			return com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION;
		}

		public Object findDeserializationConverter(Annotated a) {
			return findPostLoad(a.getRawType()) == null ? null : new MyConverter(a.getType());
		}
	}

	/**
	 * Idea borrowed from https://github.com/FasterXML/jackson-databind/issues/279
	 * Makes use of com.slavi.util.jackson.PostLoad annotation on void no-arg method.
	 * @see com.slavi.util.jackson.PostLoad
	 */
	public static void configureMapper(ObjectMapper m) {
		AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
		AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
		AnnotationIntrospector tertiary = new MyAnnotationIntrospector();
		AnnotationIntrospector pair = new AnnotationIntrospectorPair(new AnnotationIntrospectorPair(primary, secondary), tertiary);
		m.setAnnotationIntrospector(pair);

		//m.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		//m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		m.enable(SerializationFeature.INDENT_OUTPUT);
		m.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
		m.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
		m.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
		m.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
		m.enable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
		m.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
		m.enable(JsonParser.Feature.ALLOW_MISSING_VALUES);
		m.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);

		m.findAndRegisterModules();
	}

	public static ObjectMapper xmlMapper() {
		ObjectMapper m = new XmlMapper();
		configureMapper(m);
		m.registerModule(new MatrixXmlModule());
		return m;
	}

	public static ObjectMapper jsonMapper() {
		ObjectMapper m = new ObjectMapper();
		configureMapper(m);
		m.registerModule(new MatrixJsonModule());
		return m;
	}
}
