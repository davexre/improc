package com.slavi.io;

import java.beans.IntrospectionException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Properties;

public class SimpleObjectToProperties {
	private static boolean hasPropertiesStartingWith(Properties properties, String prefix) {
		prefix = getChildPrefix(prefix, "");
		for (Object key : properties.keySet()) {
			if (((String) key).startsWith(prefix))
				return true;
		}
		return false;
	}
	
	private static String getChildPrefix(String parentPrefix, String childName) {
		if ("".equals(parentPrefix))
			return childName;
		if (parentPrefix.endsWith("."))
			return parentPrefix + childName;
		return parentPrefix + "." + childName;
	}

	private static Object propertiesToObjectArray(Properties properties, String prefix, Class arrayType, boolean setToNullMissingProperties) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException {
		// array
		String arraySizeStr = properties.getProperty(getChildPrefix(prefix, "size"));
		if (arraySizeStr == null)
			return null;
		int arraySize = Integer.parseInt(arraySizeStr);
		if (arraySize < 0)
			return null;
		
		Object array = Array.newInstance(arrayType, arraySize);
		for (int i = 0; i < arraySize; i++) {
			String itemPrefix = getChildPrefix(prefix, Integer.toString(i));
			Object item = propertiesToObject(properties, itemPrefix, arrayType, setToNullMissingProperties);
			if (item == null) {
				if (arrayType.isPrimitive() || (!setToNullMissingProperties))
					continue;
			}
			Array.set(array, i, item);
		}
		return array;
	}
	
	public static <T> T propertiesToObject(Properties properties, String prefix, Class<T> objectClass, boolean setToNullMissingProperties) throws IntrospectionException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String sval = properties.getProperty(prefix);
		if ((objectClass == boolean.class) ||
			(objectClass == Boolean.class)) {
			if (sval != null)
				return (T) new Boolean(sval);
		} else if (
				(objectClass == byte.class) ||
				(objectClass == Byte.class)) {
			if (sval != null)
				return (T) new Byte(sval);
		} else if (objectClass == Character.class) {
			if ((sval != null) && (sval.length() > 0))
				return (T) new Character(sval.charAt(0));
		} else if ( 
				(objectClass == double.class) ||
				(objectClass == Double.class)) {
			if (sval != null)
				return (T) new Double(sval);
		} else if ((objectClass.isEnum()) || (objectClass == Enum.class)) {
			if (sval != null)
				return (T) Enum.valueOf((Class<Enum>) objectClass, sval);
		} else if (
				(objectClass == float.class) ||
				(objectClass == Float.class)) {
			if (sval != null)
				return (T) new Float(sval);
		} else if (
				(objectClass == int.class) ||
				(objectClass == Integer.class)) {
			if (sval != null)
				return (T) new Integer(sval);
		} else if (
				(objectClass == long.class) ||
				(objectClass == Long.class)) {
			if (sval != null)
				return (T) new Long(sval);
		} else if (
				(objectClass == short.class) ||
				(objectClass == Short.class)) {
			if (sval != null)
				return (T) new Short(sval);
		} else if (objectClass == String.class) {
			if (sval != null)
				return (T) sval;
		} else if (objectClass.getComponentType() != null) {
			// array
			Class arrayType = objectClass.getComponentType();
			return (T) propertiesToObjectArray(properties, prefix, arrayType, setToNullMissingProperties);
		}
		
		if (!hasPropertiesStartingWith(properties, prefix) && setToNullMissingProperties) {
			return null;
		}
		T object = objectClass.newInstance();
		
		Class currClass = objectClass;
		while (currClass != null) {
			for (Field field : currClass.getDeclaredFields()) {
				int modifiers = field.getModifiers();
				if (Modifier.isFinal(modifiers))
					continue;
				if (Modifier.isTransient(modifiers))
					continue;
				if (Modifier.isStatic(modifiers))
					continue;
				
				Class fieldClass = field.getType();
				Object value = propertiesToObject(properties, getChildPrefix(prefix, field.getName()), fieldClass, setToNullMissingProperties);
				if (value == null) {
					if (fieldClass.isPrimitive() || (!setToNullMissingProperties))
						continue;
				}
				field.setAccessible(true);
				field.set(object, value);
			}
			currClass = currClass.getSuperclass();
		}
		return object;
	}

	public static void objectToProperties(Properties properties, String prefix, Object object) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (object == null)
			return;
		Class objectClass = object.getClass();
		if (
			(objectClass == boolean.class) ||
			(objectClass == Boolean.class) ||
			(objectClass == byte.class) ||
			(objectClass == Byte.class) ||
			(objectClass == Character.class) ||
			(objectClass == double.class) ||
			(objectClass == Double.class) ||
			(objectClass == Enum.class) ||
			(objectClass.isEnum()) ||
			(objectClass == float.class) ||
			(objectClass == Float.class) ||
			(objectClass == int.class) ||
			(objectClass == Integer.class) ||
			(objectClass == long.class) ||
			(objectClass == Long.class) ||
			(objectClass == short.class) ||
			(objectClass == Short.class) ||
			(objectClass == String.class)) {
			properties.setProperty(prefix, object.toString());
		} else if (objectClass.getComponentType() != null) {
			// array
			int length = Array.getLength(object);
			properties.setProperty(getChildPrefix(prefix, "size"), Integer.toString(length));
			for (int i = 0; i < length; i++) {
				Object o = Array.get(object, i);
				String itemPrefix = getChildPrefix(prefix, Integer.toString(i));
				objectToProperties(properties, itemPrefix, o);
			}
		} else {
			Class currClass = objectClass;
			while (currClass != null) {
				for (Field field : currClass.getDeclaredFields()) {
					int modifiers = field.getModifiers();
					if (Modifier.isFinal(modifiers))
						continue;
					if (Modifier.isTransient(modifiers))
						continue;
					if (Modifier.isStatic(modifiers))
						continue;
					field.setAccessible(true);
					Object value = field.get(object);
					objectToProperties(properties, getChildPrefix(prefix, field.getName()), value);
				}
				currClass = currClass.getSuperclass();
			}
		}
	}
}
