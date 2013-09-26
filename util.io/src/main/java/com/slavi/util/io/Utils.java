package com.slavi.util.io;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Utils {
	public static final Map<String, Class> primitiveClasses;
	
	private static void addPrimitiveClass(Class clazz) {
		primitiveClasses.put(clazz.getName(), clazz);
	}
	
	static {
		primitiveClasses = new HashMap<String, Class>();
		addPrimitiveClass(boolean.class);
		addPrimitiveClass(Boolean.class);
		addPrimitiveClass(byte.class);
		addPrimitiveClass(Byte.class);
		addPrimitiveClass(char.class);
		addPrimitiveClass(Character.class);
		addPrimitiveClass(double.class);
		addPrimitiveClass(Double.class);
		addPrimitiveClass(float.class);
		addPrimitiveClass(Float.class);
		addPrimitiveClass(int.class);
		addPrimitiveClass(Integer.class);
		addPrimitiveClass(long.class);
		addPrimitiveClass(Long.class);
		addPrimitiveClass(short.class);
		addPrimitiveClass(Short.class);
		addPrimitiveClass(String.class);
		addPrimitiveClass(Enum.class);
	}
	
	public static boolean hasPropertiesStartingWith(Properties properties, String prefix) {
		prefix = getChildPrefix(prefix, "");
		for (Object key : properties.keySet()) {
			if (((String) key).startsWith(prefix))
				return true;
		}
		return false;
	}
	
	public static String getChildPrefix(String parentPrefix, String childName) {
		if ("".equals(parentPrefix))
			return childName;
		if (parentPrefix.endsWith("."))
			return parentPrefix + childName;
		return parentPrefix + "." + childName;
	}
	
	public static Class getClassFromClassTag(String className) throws ClassNotFoundException {
		Class r = Utils.primitiveClasses.get(className);
		if (r != null) {
			return r;
		}
		if (className.startsWith("[") && className.endsWith("]")) {
			String arrayType = className.substring(1, className.length() - 1);
			r = getClassFromClassTag(arrayType);
			r = Array.newInstance(r, 0).getClass();
			return r;
		}
		return Class.forName(className);
	}
	
	public static String computeClassTag(Class clazz) {
		if (clazz.getComponentType() != null) {
			return "[" + computeClassTag(clazz.getComponentType()) + "]";
		}
		return clazz.getName();
	}

	public static boolean isClassTagNeeded(Class clazz) {
		if (Utils.primitiveClasses.containsKey(clazz.getName()) || clazz.isEnum()) {
			// ex: public int myField;
			return false;
		}
		if (clazz.getComponentType() == null) {
			if (Modifier.isFinal(clazz.getModifiers())) {
				return false;
			}
		} else {
			// arrays are always final. check the ComponentType
			// ex: public String myField[];
			return isClassTagNeeded(clazz.getComponentType());
		}
		return true;
	}
}
