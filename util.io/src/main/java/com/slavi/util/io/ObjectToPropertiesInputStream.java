package com.slavi.util.io;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectStreamField;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;

import sun.reflect.ReflectionFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.slavi.util.io.ObjectToPropertiesOutputStream.State;

public class ObjectToPropertiesInputStream extends ObjectInputStream {
//implements ObjectInput {

	Properties properties;
	
	Stack<State> stack = new Stack<State>();

	boolean setToNullMissingProperties;

	public ObjectToPropertiesInputStream(Properties properties, String prefix) throws IOException {
		this(properties, prefix, true);
	}
	
	public ObjectToPropertiesInputStream(Properties properties, String prefix, boolean setToNullMissingProperties) throws IOException {
		this.properties = properties;
		this.setToNullMissingProperties = setToNullMissingProperties;
		stack.push(new State(prefix));
	}

	private String getProperty(String key) {
		return properties.getProperty(key);
	}

	private String getNonNullProperty() throws IOException {
		String v = properties.getProperty(getPropertyKey());
		if (v == null)
			throw new IOException("Missing non null value");
		return v;
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
	
	public int read() throws IOException {
		return Integer.parseInt(getNonNullProperty());
	}

	public int read(byte[] buf) throws IOException {
		if (buf == null) {
			throw new NullPointerException();
		}
		String v = getNonNullProperty();
		StringTokenizer st = new StringTokenizer(v);
		int max = Math.min(buf.length, st.countTokens());
		for (int i = 0; i < max; i++) {
			buf[i] = Byte.parseByte(st.nextToken());
		}
		return max;
	}

	public int read(byte[] buf, int off, int len) throws IOException {
		if (buf == null) {
			throw new NullPointerException();
		}
		int endoff = off + len;
		if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
			throw new IndexOutOfBoundsException();
		}
		String v = getNonNullProperty();
		StringTokenizer st = new StringTokenizer(v);
		len = Math.min(len, st.countTokens());
		for (int i = 0; i < len; i++) {
			buf[off + i] = Byte.parseByte(st.nextToken());
		}
		return len;
	}
	
	public int available() throws IOException {
		return 1;
	}
	
	public void close() throws IOException {
	}

	public boolean readBoolean() throws IOException {
		return Boolean.parseBoolean(getNonNullProperty());
	}
	
	public byte readByte() throws IOException  {
		return Byte.parseByte(getNonNullProperty());
	}
	
	public int readUnsignedByte() throws IOException {
		return Byte.parseByte(getNonNullProperty());
	}
	
	public char readChar() throws IOException {
		String v = getNonNullProperty();
		if ("".equals(v))
			throw new IOException("Missing non null value");
		return v.charAt(0);
	}
	
	public short readShort() throws IOException {
		return Short.parseShort(getNonNullProperty());
	}
	
	public int readUnsignedShort() throws IOException {
		return Short.parseShort(getNonNullProperty());
	}
	
	public int readInt() throws IOException {
		return Integer.parseInt(getNonNullProperty());
	}
	
	public long readLong() throws IOException {
		return Long.parseLong(getNonNullProperty());
	}
	
	public float readFloat() throws IOException {
		return Float.parseFloat(getNonNullProperty());
	}
	
	public double readDouble() throws IOException {
		return Double.parseDouble(getNonNullProperty());
	}
	
	public void readFully(byte[] buf) throws IOException {
		if (buf == null) {
			throw new NullPointerException();
		}
		String v = getNonNullProperty();
		StringTokenizer st = new StringTokenizer(v);
		int max = Math.min(buf.length, st.countTokens());
		for (int i = 0; i < max; i++) {
			buf[i] = Byte.parseByte(st.nextToken());
		}
		for (int i = max; i < buf.length; i++)
			buf[i] = 0;
	}
	
	public void readFully(byte[] buf, int off, int len) throws IOException {
		if (buf == null) {
			throw new NullPointerException();
		}
		int endoff = off + len;
		if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
			throw new IndexOutOfBoundsException();
		}
		String v = getNonNullProperty();
		StringTokenizer st = new StringTokenizer(v);
		int max = Math.min(len, st.countTokens());
		for (int i = 0; i < max; i++) {
			buf[off + i] = Byte.parseByte(st.nextToken());
		}
		for (int i = max; i < len; i++)
			buf[i] = 0;
	}
	
	public long skip(long n) throws IOException {
		return 0;
	}
	public int skipBytes(int len) throws IOException {
		return 0;
	}
	
	public String readLine() throws IOException {
		return getNonNullProperty();
	}
	
	public String readUTF() throws IOException {
		return getNonNullProperty();
	}
	
	public void defaultReadObject() throws IOException, ClassNotFoundException {
		if (stack.peek().curObject == null) {
			throw new NotActiveException("not in call to readObject");
		}
		State state = stack.peek();
		Object curObject = state.curObject;
		Class curClass = state.curClass;
		state.curObject = null;
		state.curClass = null;
		try {
			defaultReadFields(curObject, curClass);
		} catch (ClassNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	protected Object readObjectOverride() throws IOException, ClassNotFoundException {
		State state = stack.peek();
		String newPrefix = Utils.getChildPrefix(state.prefix, "$object$" + Integer.toString(++state.objectCounter));
		pushPrefix(newPrefix);
		try {
			String objectClassName = getProperty(Utils.getChildPrefix(stack.peek().prefix, "$class"));
			if (objectClassName == null)
				return null;
			return readObjectOverride0(objectClassName);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			popPrefix();
		}
	}
	
	public ArrayList handle = new ArrayList();

	protected Object readObjectOverride0(String objectClassName) throws Exception {
		if (objectClassName == null)
			return null;
		if (objectClassName.startsWith("$ref$")) {
			int index = Integer.parseInt(objectClassName.substring("$ref$".length()));
			Object o = handle.get(index - 1);
			return o;
		}
		// TC_CLASSDESC:
		// TC_PROXYCLASSDESC:
		
		Class objectClass = Utils.getClassFromClassTag(objectClassName);
		if (objectClass.getComponentType() != null) {
			// array
			Class arrayType = objectClass.getComponentType();
			String arrayItemType = objectClassName.substring(1, objectClassName.length() - 1);
			String arraySizeStr = getProperty(Utils.getChildPrefix(stack.peek().prefix, "$size"));
			if (arraySizeStr == null)
				return null;
			int arraySize = Integer.parseInt(arraySizeStr);
			if (arraySize < 0)
				return null;
			Object array = Array.newInstance(arrayType, arraySize);
			handle.add(array);

			boolean arrayItemsNeedClassTag = Utils.isClassTagNeeded(objectClass);
			for (int i = 0; i < arraySize; i++) {
				String itemPrefix = Utils.getChildPrefix(stack.peek().prefix, Integer.toString(i));
				pushPrefix(itemPrefix);
				Object item;
				if (arrayItemsNeedClassTag)
					item = readObjectOverride();
				else
					item = readObjectOverride0(arrayItemType);
				if (item == null) {
					if (arrayType.isPrimitive() || (!setToNullMissingProperties))
						continue;
				}
				Array.set(array, i, item);
				popPrefix();
			}
			return array;
		}
		
		String sval = getProperty(stack.peek().prefix);
		if ((objectClass == boolean.class) ||
			(objectClass == Boolean.class)) {
			return (sval == null) ? null : new Boolean(sval);
		}
		if ((objectClass == byte.class) ||
			(objectClass == Byte.class)) {
			return (sval == null) ? null : new Byte(sval);
		}
		if ((objectClass == char.class) ||
			(objectClass == Character.class)) {
			return ((sval != null) && (sval.length() > 0)) ? null : new Character(sval.charAt(0));
		}
		if ((objectClass == double.class) ||
			(objectClass == Double.class)) {
			return (sval == null) ? null : new Double(sval);
		}
		if ((objectClass.isEnum()) || (objectClass == Enum.class)) {
			return (sval == null) ? null : Enum.valueOf((Class<Enum>) objectClass, sval);
		}
		if ((objectClass == float.class) ||
			(objectClass == Float.class)) {
			return (sval == null) ? null : new Float(sval);
		}
		if ((objectClass == int.class) ||
			(objectClass == Integer.class)) {
			return (sval == null) ? null : new Integer(sval);
		}
		if ((objectClass == long.class) ||
			(objectClass == Long.class)) {
			return (sval == null) ? null : new Long(sval);
		}
		if ((objectClass == short.class) ||
			(objectClass == Short.class)) {
			return (sval == null) ? null : new Short(sval);
		}
		if (objectClass == String.class) {
			return (sval == null) ? null : sval;
		}
		if (objectClass == Class.class) {
			Class r = Utils.getClassFromClassTag(sval);
			return r;
		}

		return readOrdinaryObject(objectClass);
	}
	
	protected Object readOrdinaryObject(Class objectClass) throws Exception {
		if (!Utils.hasPropertiesStartingWith(properties, stack.peek().prefix) && setToNullMissingProperties) {
			return null;
		}

		Constructor intConstr = null;
		if (Externalizable.class.isAssignableFrom(objectClass)) {
			try {
				intConstr = objectClass.getDeclaredConstructor((Class[]) null);
			} catch (NoSuchMethodException ex) {
				intConstr = null;
			}
		}
		if (intConstr == null) {
			/*
			 * Creating new object WITHOUT invoking a constructor.
			 * Ideas borrowed from http://www.javaspecialists.eu/archive/Issue175.html
			 */
			ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
			Constructor objDef = Object.class.getDeclaredConstructor();
			intConstr = rf.newConstructorForSerialization(objectClass, objDef);
		}
		Object object = intConstr.newInstance();
		handle.add(object);
		
		State state = stack.peek();
		if (Externalizable.class.isAssignableFrom(objectClass)) {
			state.curClass = objectClass;
			state.curObject = object;
			((Externalizable) object).readExternal(this);
			state.curClass = null;
			state.curObject = null;
			return object;
		}
		
		Class curClass = objectClass;
		while (curClass != null) {
			Method readObjectMethod;
			try {
				readObjectMethod = curClass.getDeclaredMethod("readObject", new Class[] { ObjectInputStream.class });
			} catch (NoSuchMethodException e) {
				readObjectMethod = null;
			}
			if (readObjectMethod != null) {
				readObjectMethod.setAccessible(true);
				state.curClass = curClass;
				state.curObject = object;
				readObjectMethod.invoke(object, new Object[] { this });
				state.curClass = null;
				state.curObject = null;
			} else {
				defaultReadFields(object, curClass);
			}
			curClass = curClass.getSuperclass();
			state.prefix = Utils.getChildPrefix(state.prefix, "$");
		}
		return object;
	}
	
	protected void defaultReadFields(Object object, Class objectClass) throws Exception {
		ObjectStreamField[] fields = ObjectToPropertiesOutputStream.getSerialFields(objectClass);
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
			pushPrefix(Utils.getChildPrefix(stack.peek().prefix, osfield.getName()));
			try {
				String classStr = getProperty(Utils.getChildPrefix(stack.peek().prefix, "$class"));
				if (classStr == null)
					classStr = Utils.computeClassTag(fieldType);
				Object value = readObjectOverride0(classStr);
				if (value == null) {
					if (fieldType.isPrimitive() || (!setToNullMissingProperties))
						continue;
				}
				field.set(object, value);
			} finally {
				popPrefix();
			}
		}
	}
	
	public Object readUnshared() throws IOException, ClassNotFoundException {
		return readObjectOverride();
	}
	
	public ObjectInputStream.GetField readFields()
			throws IOException, ClassNotFoundException {
		throw new NotImplementedException();
	}

	public void registerValidation(ObjectInputValidation obj, int prio)
			throws NotActiveException, InvalidObjectException {
		throw new NotImplementedException();
	}
}
