package com.slavi.io;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

public class ObjectToPropertiesOutputStream extends ObjectOutputStream {

	Properties properties;

	String prefix;
	
	Stack<Integer> stack = new Stack<Integer>();
	int counter = 0;

	int rootObjectCounter;
	
	public ObjectToPropertiesOutputStream(Properties properties, String prefix) throws IOException {
		this.properties = properties;
		this.prefix = prefix;
	}

	private void pushPrefix(String subPrefix) {
		prefix = Utils.getChildPrefix(prefix, subPrefix);
		stack.push(counter);
		counter = 0;
	}
	
	private void popPrefix() {
		counter = stack.pop();
		int last = prefix.lastIndexOf('.');
		prefix = prefix.substring(0, last);
	}
	
	private String getPropertyKey() {
		return Utils.getChildPrefix(prefix, "$$" + Integer.toString(++counter));
	}
	
	public void writeBoolean(boolean v) throws IOException {
		properties.setProperty(getPropertyKey(), Boolean.toString(v));
	}

	public void writeByte(int v) throws IOException {
		properties.setProperty(getPropertyKey(), Byte.toString((byte) v));
	}

	public void writeShort(int v) throws IOException {
		properties.setProperty(getPropertyKey(), Short.toString((short) v));
	}

	public void writeChar(int v) throws IOException {
		properties.setProperty(getPropertyKey(), new String(new int[] { v }, 0, 1));
	}

	public void writeInt(int v) throws IOException {
		properties.setProperty(getPropertyKey(), Integer.toString(v));
	}

	public void writeLong(long v) throws IOException {
		properties.setProperty(getPropertyKey(), Long.toString(v));
	}

	public void writeFloat(float v) throws IOException {
		properties.setProperty(getPropertyKey(), Float.toString(v));
	}

	public void writeDouble(double v) throws IOException {
		properties.setProperty(getPropertyKey(), Double.toString(v));
	}

	public void writeBytes(String s) throws IOException {
		properties.setProperty(getPropertyKey(), s);
	}

	public void writeChars(String s) throws IOException {
		properties.setProperty(getPropertyKey(), s);
	}

	public void writeUTF(String s) throws IOException {
		properties.setProperty(getPropertyKey(), s);
	}

	public void write(int b) throws IOException {
		properties.setProperty(getPropertyKey(), Integer.toString(b));
	}

	public void write(byte[] b) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			sb.append(b[i]);
			sb.append(' ');
		}
		properties.setProperty(getPropertyKey(), sb.toString());
	}

	public void write(byte[] b, int off, int len) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			sb.append(b[off + i]);
			sb.append(' ');
		}
		properties.setProperty(getPropertyKey(), sb.toString());
	}

	public void flush() throws IOException {
	}

	public void close() throws IOException {
	}

	Map<Object, Object> subs = new HashMap<Object, Object>();
	public Map<Object, Integer> handles = new HashMap<Object, Integer>();

	protected void writeObjectOverride(Object obj) throws IOException {
		pushPrefix("$object$" + Integer.toString(++rootObjectCounter));
		try {
			writeObjectOverride0(obj);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	protected void writeObjectOverride0(Object obj) throws Exception {
		// handle previously written and non-replaceable objects
		Integer h;
		if (subs.containsKey(obj))
			obj = subs.get(obj);
		if (obj == null) {
			rootObjectCounter++;
			return;
		} else if ((h = handles.get(obj)) != null) {
			properties.setProperty(Utils.getChildPrefix(prefix, "$class"), "$ref$" + h.toString());
			return;
		} else if (obj instanceof Class) {
			//writeClass((Class) obj);
			return;
		} else if (obj instanceof ObjectStreamClass) {
			//writeClassDesc((ObjectStreamClass) obj);
			return;
		}

		// check for replacement object
		Object orig = obj;
		Class cl = obj.getClass();

		Method writeReplace = null;
		Class defCl = cl;
		while (defCl != null) {
			try {
				writeReplace = defCl.getDeclaredMethod("writeReplace", (Class[]) null);
				break;
			} catch (NoSuchMethodException ex) {
				defCl = defCl.getSuperclass();
			}
		}

		for (;;) {
			// REMIND: skip this check for strings/arrays?
			Class repCl;

			if ((writeReplace == null) || (obj = writeReplace.invoke(obj, (Object[]) null)) == null 
					|| (obj == null) || (repCl = obj.getClass()) == cl) {
				break;
			}
			cl = repCl;
		}

		obj = replaceObject(obj);
		// if object replaced, run through original checks a second time
		if (obj != orig) {
			subs.put(orig, obj);
			if (obj == null) {
				rootObjectCounter++;
				return;
			} else if ((h = handles.get(obj)) != null) {
				properties.setProperty(Utils.getChildPrefix(prefix, "$class"), "$ref$" + h.toString());
				return;
			} else if (obj instanceof Class) {
				//writeClass((Class) obj);
				return;
			} else if (obj instanceof ObjectStreamClass) {
				//writeClassDesc((ObjectStreamClass) obj);
				return;
			}
		}

		// remaining cases
		if (obj instanceof String) {
			properties.setProperty(prefix, obj.toString());
		} else if (cl.isArray()) {
			writeArray(obj);
		} else if (obj instanceof Enum) {
			properties.setProperty(prefix, obj.toString());
		} else if (obj instanceof Serializable) {
			//writeOrdinaryObject(obj, desc);
		} else {
			throw new NotSerializableException(cl.getName());
		}
	}
	
	private void writeArray(Object array) throws Exception {
		Class objectClass = array.getClass();
		String classTag = ObjectToProperties2.computeClassTag(objectClass);
		properties.setProperty(Utils.getChildPrefix(prefix, "$class"), classTag);
		handles.put(array, handles.size() + 1);

		Class ccl = objectClass.getComponentType();
		if (ccl.isPrimitive()) {
			StringBuilder sb = new StringBuilder();
			if (ccl == Integer.TYPE) {
				int[] arr = (int[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					sb.append(' ');
				}
				properties.setProperty(prefix, sb.toString());
			} else if (ccl == Byte.TYPE) {
				byte[] arr = (byte[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					sb.append(' ');
				}
				properties.setProperty(prefix, sb.toString());
			} else if (ccl == Long.TYPE) {
				long[] arr = (long[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					sb.append(' ');
				}
				properties.setProperty(prefix, sb.toString());
			} else if (ccl == Float.TYPE) {
				float[] arr = (float[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					sb.append(' ');
				}
				properties.setProperty(prefix, sb.toString());
			} else if (ccl == Double.TYPE) {
				double[] arr = (double[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					sb.append(' ');
				}
				properties.setProperty(prefix, sb.toString());
			} else if (ccl == Short.TYPE) {
				short[] arr = (short[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					sb.append(' ');
				}
				properties.setProperty(prefix, sb.toString());
			} else if (ccl == Character.TYPE) {
				properties.setProperty(prefix, new String((char[]) array));
			} else if (ccl == Boolean.TYPE) {
				boolean[] arr = (boolean[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i] ? '1' : '0');
				}
				properties.setProperty(prefix, sb.toString());
			} else {
				throw new InternalError();
			}
		} else {
			Object[] objs = (Object[]) array;
			int len = objs.length;
			properties.setProperty(Utils.getChildPrefix(prefix, "$size"), Integer.toString(len));
			for (int i = 0; i < len; i++) {
				pushPrefix("0");
				writeObjectOverride0(objs[i]);
				popPrefix();
			}
		}
	}
	
}
