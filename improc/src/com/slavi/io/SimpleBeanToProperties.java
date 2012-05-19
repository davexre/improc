package com.slavi.io;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class SimpleBeanToProperties {
	private static boolean hasPropertiesStartingWith(Properties properties, String prefix) {
		if (!prefix.endsWith("."))
			prefix += ".";
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
	
	private static Object propertiesToObjectArray(Properties properties, String prefix, Class arrayType) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException {
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
			if (hasPropertiesStartingWith(properties, itemPrefix)) {
				Object item = propertiesToObject(properties, itemPrefix, arrayType);
				Array.set(array, i, item);
			}
		}
		return array;
	}
	
	public static <T> T propertiesToObject(Properties properties, String prefix, Class<T> objectClass) throws IntrospectionException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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
			return (T) propertiesToObjectArray(properties, prefix, arrayType);
		} else if (Serializable.class.isAssignableFrom(objectClass)) {
			if (!hasPropertiesStartingWith(properties, prefix)) {
				return null;
			}
			Serializable object = (Serializable) objectClass.getConstructor((Class[]) null).newInstance((Object[]) null);
			
			BeanInfo beanInfo = Introspector.getBeanInfo(objectClass);
			PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				if (pd instanceof IndexedPropertyDescriptor) {
					continue;
				}
				
				Method write = pd.getWriteMethod();
				if (write == null)
					continue;

				Class propertyType = pd.getPropertyType();
				String propertyPrefix = getChildPrefix(prefix, pd.getName());
				if (hasPropertiesStartingWith(properties, propertyPrefix)) {
					Object o = propertiesToObject(properties, propertyPrefix, propertyType);
					write.invoke(object, new Object[] { o });
				}
			}
			
			for (PropertyDescriptor pd : pds) {
				if (!(pd instanceof IndexedPropertyDescriptor)) {
					continue;
				}
				Method read = pd.getReadMethod();
				if (read == null)
					continue;

				IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
				if ((ipd.getWriteMethod() == null) && (ipd.getIndexedWriteMethod() == null))
					continue;
				
				Class propertyType = pd.getPropertyType();
				String propertyPrefix = getChildPrefix(prefix, pd.getName());
				if (hasPropertiesStartingWith(properties, propertyPrefix)) {
					Object items = propertiesToObject(properties, propertyPrefix, propertyType);

					if (ipd.getWriteMethod() != null) {
						Method write = ipd.getWriteMethod();
						write.invoke(object, new Object[] { items });
					} else if (items != null) {
						Method write = ipd.getIndexedWriteMethod();
						int length = Array.getLength(items);
						for (int i = 0; i < length; i++) {
							Object item = Array.get(items, i);
							write.invoke(object, new Object[] { i, item });
						}
					}
				}
			}
			
			return (T) object;
		}
		return null;
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
		} else if (Serializable.class.isAssignableFrom(objectClass)) {
			BeanInfo beanInfo = Introspector.getBeanInfo(objectClass);
			PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				Method read = pd.getReadMethod();
				if (read == null)
					continue;
				
				if (pd instanceof IndexedPropertyDescriptor) {
					IndexedPropertyDescriptor indexedProperty = (IndexedPropertyDescriptor) pd;
					if ((pd.getWriteMethod() == null) && (indexedProperty.getIndexedWriteMethod() == null))
						continue;
					
					Object array = read.invoke(object, (Object[]) null);
					objectToProperties(properties, getChildPrefix(prefix, indexedProperty.getName()), array);
					continue;
				} else {
					if (pd.getWriteMethod() == null)
						continue;
					Object value = read.invoke(object, (Object[]) null);
					objectToProperties(properties, getChildPrefix(prefix, pd.getName()), value);
				}
			}
		}
	}
}
