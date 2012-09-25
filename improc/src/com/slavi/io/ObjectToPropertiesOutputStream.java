package com.slavi.io;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

public class ObjectToPropertiesOutputStream extends ObjectOutputStream {

	Properties properties;

	static class State {
		public String prefix;
		
		public Object curObject = null;
		public Class curClass = null;
		
		public int objectCounter = 0;
		public int fieldCounter = 0;
		
		public State(String prefix) {
			this.prefix = prefix;
		}
	}
	
	Stack<State> stack = new Stack<State>();

	public ObjectToPropertiesOutputStream(Properties properties, String prefix) throws IOException {
		this.properties = properties;
		stack.push(new State(prefix));
	}

	private void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	private void pushPrefix(String prefix) {
		stack.push(new State(prefix));
	}
	
	private void popPrefix() {
		if (stack.size() <= 1)
			return;
		stack.pop();
	}
	
	private String getPropertyKey() {
		State state = stack.peek();
		return Utils.getChildPrefix(state.prefix, "_$" + Integer.toString(++state.fieldCounter));
	}
	
	public void writeBoolean(boolean v) throws IOException {
		setProperty(getPropertyKey(), Boolean.toString(v));
	}

	public void writeByte(int v) throws IOException {
		setProperty(getPropertyKey(), Byte.toString((byte) v));
	}

	public void writeShort(int v) throws IOException {
		setProperty(getPropertyKey(), Short.toString((short) v));
	}

	public void writeChar(int v) throws IOException {
		setProperty(getPropertyKey(), new String(new int[] { v }, 0, 1));
	}

	public void writeInt(int v) throws IOException {
		setProperty(getPropertyKey(), Integer.toString(v));
	}

	public void writeLong(long v) throws IOException {
		setProperty(getPropertyKey(), Long.toString(v));
	}

	public void writeFloat(float v) throws IOException {
		setProperty(getPropertyKey(), Float.toString(v));
	}

	public void writeDouble(double v) throws IOException {
		setProperty(getPropertyKey(), Double.toString(v));
	}

	public void writeBytes(String s) throws IOException {
		setProperty(getPropertyKey(), s);
	}

	public void writeChars(String s) throws IOException {
		setProperty(getPropertyKey(), s);
	}

	public void writeUTF(String s) throws IOException {
		setProperty(getPropertyKey(), s);
	}

	public void write(int b) throws IOException {
		setProperty(getPropertyKey(), Integer.toString(b));
	}

	public void write(byte[] b) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			sb.append(b[i]);
			sb.append(' ');
		}
		setProperty(getPropertyKey(), sb.toString());
	}

	public void write(byte[] b, int off, int len) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			sb.append(b[off + i]);
			sb.append(' ');
		}
		setProperty(getPropertyKey(), sb.toString());
	}

	public void flush() throws IOException {
	}

	public void close() throws IOException {
	}

	Map<Object, Object> subs = new HashMap<Object, Object>();
	public Map<Object, Integer> handles = new HashMap<Object, Integer>();

	public void defaultWriteObject() throws IOException {
		if (stack.peek().curObject == null) {
			throw new NotActiveException("not in call to writeObject");
		}
		State state = stack.peek();
		Object curObject = state.curObject;
		Class curClass = state.curClass;
		state.curObject = null;
		state.curClass = null;
		try {
			defaultWriteFields(curObject, curClass);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	protected void writeObjectOverride(Object obj) throws IOException {
		State state = stack.peek();
		String newPrefix = Utils.getChildPrefix(state.prefix, "$object$" + Integer.toString(++state.objectCounter));
		pushPrefix(newPrefix);
		try {
			writeObjectOverride0(obj, true);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			popPrefix();
		}
	}
	
	protected void writeObjectOverride0(Object obj, boolean needsClassTag) throws Exception {
		// handle previously written and non-replaceable objects
		Integer h;
		if (subs.containsKey(obj))
			obj = subs.get(obj);
		if (obj == null) {
			getPropertyKey();
			return;
		} else if ((h = handles.get(obj)) != null) {
			setProperty(Utils.getChildPrefix(stack.peek().prefix, "$class"), "$ref$" + h.toString());
			return;
		} else if (obj instanceof Class) {
			writeClass((Class) obj, needsClassTag);
			return;
//		} else if (obj instanceof ObjectStreamClass) {
//			//writeClassDesc((ObjectStreamClass) obj);
//			return;
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
				getPropertyKey();
				return;
			} else if ((h = handles.get(obj)) != null) {
				setProperty(Utils.getChildPrefix(stack.peek().prefix, "$class"), "$ref$" + h.toString());
				return;
			} else if (obj instanceof Class) {
				writeClass((Class) obj, needsClassTag);
				return;
//			} else if (obj instanceof ObjectStreamClass) {
//				//writeClassDesc((ObjectStreamClass) obj);
//				return;
			}
		}

		// remaining cases
		Class objectClass = obj.getClass();
		String objectClassName = ObjectToProperties2.computeClassTag(objectClass);
		if (cl.isArray()) {
			writeArray(obj, needsClassTag);
		} else if (Utils.primitiveClasses.containsKey(objectClassName) || objectClass.isEnum()) {
			if (needsClassTag) {
				setProperty(Utils.getChildPrefix(stack.peek().prefix, "$class"), objectClassName);
			}
			setProperty(stack.peek().prefix, obj.toString());
		} else if (obj instanceof Serializable) {
			writeOrdinaryObject(obj, needsClassTag);
		} else {
//			writeOrdinaryObject(obj, needsClassTag);
			throw new NotSerializableException(cl.getName());
		}
	}

	/**
	 * Returns serializable fields of given class as defined explicitly by a
	 * "serialPersistentFields" field, or null if no appropriate
	 * "serialPersistentFields" field is defined.  Serializable fields backed
	 * by an actual field of the class are represented by ObjectStreamFields
	 * with corresponding non-null Field objects.  For compatibility with past
	 * releases, a "serialPersistentFields" field with a null value is
	 * considered equivalent to not declaring "serialPersistentFields".  Throws
	 * InvalidClassException if the declared serializable fields are
	 * invalid--e.g., if multiple fields share the same name.
	 */
	private static ObjectStreamField[] getDeclaredSerialFields(Class cl) throws InvalidClassException {
		ObjectStreamField[] serialPersistentFields = null;
		try {
			Field f = cl.getDeclaredField("serialPersistentFields");
			int mask = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
			if ((f.getModifiers() & mask) == mask) {
				f.setAccessible(true);
				serialPersistentFields = (ObjectStreamField[]) f.get(null);
			}
		} catch (Exception ex) {
		}
		if (serialPersistentFields == null) {
			return null;
		} else if (serialPersistentFields.length == 0) {
			return ObjectStreamClass.NO_FIELDS;
		}

		Set fieldNames = new HashSet(serialPersistentFields.length);
		for (int i = 0; i < serialPersistentFields.length; i++) {
			ObjectStreamField spf = serialPersistentFields[i];

			String fname = spf.getName();
			if (fieldNames.contains(fname)) {
				throw new InvalidClassException("multiple serializable fields named " + fname);
			}
			fieldNames.add(fname);
		}
		return serialPersistentFields;
	}

	/**
	 * Returns array of ObjectStreamFields corresponding to all non-static
	 * non-transient fields declared by given class.  Each ObjectStreamField
	 * contains a Field object for the field it represents.  If no default
	 * serializable fields exist, NO_FIELDS is returned.
	 */
	private static ObjectStreamField[] getDefaultSerialFields(Class cl) {
		Field[] clFields = cl.getDeclaredFields();
		ArrayList list = new ArrayList();
		int mask = Modifier.STATIC | Modifier.TRANSIENT;

		for (int i = 0; i < clFields.length; i++) {
			if ((clFields[i].getModifiers() & mask) == 0) {
				list.add(new ObjectStreamField(clFields[i].getName(), clFields[i].getType(), false));
			}
		}
		int size = list.size();
		return (size == 0) ? ObjectStreamClass.NO_FIELDS : (ObjectStreamField[]) list.toArray(new ObjectStreamField[size]);
	}

	/**
	 * Returns ObjectStreamField array describing the serializable fields of
	 * the given class.  Serializable fields backed by an actual field of the
	 * class are represented by ObjectStreamFields with corresponding non-null
	 * Field objects.  Throws InvalidClassException if the (explicitly
	 * declared) serializable fields are invalid.
	 */
	static ObjectStreamField[] getSerialFields(Class cl) throws InvalidClassException {
		ObjectStreamField[] fields;
		if (Serializable.class.isAssignableFrom(cl) && !Externalizable.class.isAssignableFrom(cl)
				&& !Proxy.isProxyClass(cl) && !cl.isInterface()) {
			if ((fields = getDeclaredSerialFields(cl)) == null) {
				fields = getDefaultSerialFields(cl);
			}
			Arrays.sort(fields);
		} else {
			fields = ObjectStreamClass.NO_FIELDS;
		}
		return fields;
	}
	
	protected void defaultWriteFields(Object object, Class objectClass) throws Exception {
		/*				Field fields[] = currClass.getDeclaredFields();
		Arrays.sort(fields, new Comparator<Field>() {
			public int compare(Field f1, Field f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});
		for (Field field : fields) {
			int modifiers = field.getModifiers();
			if (Modifier.isTransient(modifiers))
				continue;
			if (Modifier.isStatic(modifiers))
				continue;
		}*/
		ObjectStreamField[] fields = getSerialFields(objectClass);
		Arrays.sort(fields);
		for (int i = 0; i < fields.length; i++) {
			ObjectStreamField osfield = fields[i];
			Field field = objectClass.getDeclaredField(osfield.getName());
			int modifiers = field.getModifiers();
			if (Modifier.isTransient(modifiers))
				continue;
			if (Modifier.isStatic(modifiers))
				continue;
			Class fieldType = field.getType();
			field.setAccessible(true);
			boolean fieldItemNeedClassTag = ObjectToProperties2.isClassTagNeeded(fieldType);
			Object value = field.get(object);
			pushPrefix(Utils.getChildPrefix(stack.peek().prefix, osfield.getName()));
			writeObjectOverride0(value, fieldItemNeedClassTag);
			popPrefix();
		}
	}
	
	protected void writeOrdinaryObject(Object object, boolean needsClassTag) throws Exception {
		Class objectClass = object.getClass();
		String objectClassName = ObjectToProperties2.computeClassTag(objectClass);
		if (needsClassTag) {
			setProperty(Utils.getChildPrefix(stack.peek().prefix, "$class"), objectClassName);
		}
		State state = stack.peek();
		if (Externalizable.class.isAssignableFrom(objectClass)) {
			state.curClass = objectClass;
			state.curObject = object;
			((Externalizable) object).writeExternal(this);
			state.curClass = null;
			state.curObject = null;
			return;
		}
		
		Class curClass = objectClass;
		while (curClass != null) {
			Method writeObjectMethod;
			try {
				writeObjectMethod = curClass.getDeclaredMethod("writeObject", new Class[] { ObjectOutputStream.class });
			} catch (NoSuchMethodException e) {
				writeObjectMethod = null;
			}
			if (writeObjectMethod != null) {
				writeObjectMethod.setAccessible(true);
				state.curClass = curClass;
				state.curObject = object;
				writeObjectMethod.invoke(object, new Object[] { this });
				state.curClass = null;
				state.curObject = null;
			} else {
				defaultWriteFields(object, curClass);
			}
			curClass = curClass.getSuperclass();
			state.prefix = Utils.getChildPrefix(state.prefix, "$");
		}
	}

	protected void writeClass(Class clazz, boolean needsClassTag) throws Exception {
		setProperty(Utils.getChildPrefix(stack.peek().prefix, "$class"), Class.class.getName()); // "java.lang.Class"
		String value = ObjectToProperties2.computeClassTag(clazz);
		setProperty(stack.peek().prefix, value);
	}
	
	protected void writeArray(Object array, boolean needsClassTag) throws Exception {
		Class objectClass = array.getClass();
		String objectClassName = ObjectToProperties2.computeClassTag(objectClass);
		if (needsClassTag) {
			setProperty(Utils.getChildPrefix(stack.peek().prefix, "$class"), objectClassName);
		}
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
				setProperty(stack.peek().prefix, sb.toString());
			} else if (ccl == Byte.TYPE) {
				byte[] arr = (byte[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					sb.append(' ');
				}
				setProperty(stack.peek().prefix, sb.toString());
			} else if (ccl == Long.TYPE) {
				long[] arr = (long[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					sb.append(' ');
				}
				setProperty(stack.peek().prefix, sb.toString());
			} else if (ccl == Float.TYPE) {
				float[] arr = (float[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					sb.append(' ');
				}
				setProperty(stack.peek().prefix, sb.toString());
			} else if (ccl == Double.TYPE) {
				double[] arr = (double[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					sb.append(' ');
				}
				setProperty(stack.peek().prefix, sb.toString());
			} else if (ccl == Short.TYPE) {
				short[] arr = (short[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
					sb.append(' ');
				}
				setProperty(stack.peek().prefix, sb.toString());
			} else if (ccl == Character.TYPE) {
				setProperty(stack.peek().prefix, new String((char[]) array));
			} else if (ccl == Boolean.TYPE) {
				boolean[] arr = (boolean[]) array;
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i] ? '1' : '0');
				}
				setProperty(stack.peek().prefix, sb.toString());
			} else {
				throw new InternalError();
			}
		} else {
			int len = Array.getLength(array);
			setProperty(Utils.getChildPrefix(stack.peek().prefix, "$size"), Integer.toString(len));
			boolean arrayItemsNeedClassTag = ObjectToProperties2.isClassTagNeeded(objectClass.getComponentType());
			for (int i = 0; i < len; i++) {
				pushPrefix(Utils.getChildPrefix(stack.peek().prefix, Integer.toString(i)));
				Object o = Array.get(array, i);
				writeObjectOverride0(o, arrayItemsNeedClassTag);
				popPrefix();
			}
		}
	}
}
