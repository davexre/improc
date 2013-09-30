package com.slavi.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Iterator;
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

/**
 * This class contains utility static methods for general purpose.  
 */
public class Util {
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
		if (Math.floor(size) == size) 
			return String.format(Locale.US, "%d %s", new Object[] { new Integer((int)size), dim } );
		return String.format(Locale.US, "%.1f %s", new Object[] { new Double(size), dim } );
	}

	/**
	 * The code bellow is borrowed from WedSphinx
	 * http://www.cs.cmu.edu/~rcm/websphinx
	 * and slightly modified 
	 * 
	 * Gets a wildcard pattern and returns a Regexp equivalent.  
	 * 
	 * Wildcards are similar to sh-style file globbing.
	 * A wildcard pattern is implicitly anchored, meaning that it must match the entire string.
	 * The wildcard operators are:
	 * <pre>
	 *    ? matches one arbitrary character
	 *    * matches zero or more arbitrary characters
	 *    [xyz] matches characters x or y or z
	 *    {foo,bar,baz}   matches expressions foo or bar or baz
	 *    ()  grouping to extract fields
	 *    \ escape one of these special characters
	 * </pre>
	 * Escape codes (like \n and \t) and Perl5 character classes (like \w and \s) may also be used.
	 */
	public static String toRegexpStr(String wildcard) {
		String s = wildcard;

		int inAlternative = 0;
		int inSet = 0;
		boolean inEscape = false;

		StringBuilder output = new StringBuilder();

		int len = s.length();
		for (int i = 0; i < len; ++i) {
			char c = s.charAt(i);
			if (inEscape) {
				output.append(c);
				inEscape = false;
			} else {
				switch (c) {
				case '\\':
					output.append(c);
					inEscape = true;
					break;
				case '?':
					output.append('.');
					break;
				case '*':
					output.append(".*");
					break;
				case '[':
					output.append(c);
					++inSet;
					break;
				case ']':
					// FIX: handle [] case properly
					output.append(c);
					--inSet;
					break;
				case '{':
					output.append("(?:");
					++inAlternative;
					break;
				case ',':
					if (inAlternative > 0)
						output.append("|");
					else
						output.append(c);
					break;
				case '}':
					output.append(")");
					--inAlternative;
					break;
				case '^':
					if (inSet > 0) {
						output.append(c);
					} else {
						output.append('\\');
						output.append(c);
					}
					break;
				case '$':
				case '.':
				case '|':
				case '+':
					output.append('\\');
					output.append(c);
					break;
				default:
					output.append(c);
					break;
				}
			}
		}
		if (inEscape)
			output.append('\\');

		return output.toString();
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
}
