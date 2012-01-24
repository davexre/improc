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
		for (Object key : properties.keySet()) {
			if (((String) key).startsWith(prefix))
				return true;
		}
		return false;
	}
	
	private static Object[] propertiesToObjectArray(Properties properties, String prefix, Class arrayType) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException {
		if (!Serializable.class.isAssignableFrom(arrayType))
			return null;
		String arraySizeStr = properties.getProperty(prefix + "size");
		if (arraySizeStr == null)
			return null;
		int arraySize = Integer.parseInt(arraySizeStr);
		if (arraySize < 0)
			return null;
		
		Object items[] = (Object[]) Array.newInstance(arrayType, arraySize);
		for (int i = 0; i < arraySize; i++) {
			String itemPrefix = prefix + Integer.toString(i) + ".";
			if (hasPropertiesStartingWith(properties, itemPrefix)) {
				items[i] = (Serializable) arrayType.getConstructor((Class[]) null).newInstance((Object[]) null);
				propertiesToObject(properties, itemPrefix, (Serializable) items[i]);
			}
		}
		return items;
	}
	
	public static <BeanObject extends Serializable> BeanObject propertiesToObject(Properties properties, String prefix, BeanObject object) throws IntrospectionException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class objectClass = object.getClass();
		BeanInfo beanInfo = Introspector.getBeanInfo(objectClass);
		PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			if (pd instanceof IndexedPropertyDescriptor) {
				continue;
			} else {
				Method write = pd.getWriteMethod();
				if (write == null)
					continue;
				String sval = properties.getProperty(prefix + pd.getName());
				Class propertyType = pd.getPropertyType();
				if (propertyType.getComponentType() != null) {
					// array
					Class arrayType = propertyType.getComponentType();
					if (Serializable.class.isAssignableFrom(arrayType)) {
						Object items[] = propertiesToObjectArray(properties, prefix + pd.getName() + ".", arrayType);
						if (items != null)
							write.invoke(object, new Object[] { items });
					}
				} else if ((propertyType == boolean.class) ||
					(propertyType == Boolean.class)) {
					if (sval != null)
						write.invoke(object, Boolean.parseBoolean(sval));
				} else if (
					(propertyType == byte.class) ||
					(propertyType == Byte.class)) {
					if (sval != null)
						write.invoke(object, Byte.parseByte(sval));
				} else if (propertyType == Character.class) {
					if ((sval != null) && (sval.length() > 0))
						write.invoke(object, sval.charAt(0));
				} else if ( 
						(propertyType == double.class) ||
						(propertyType == Double.class)) {
					if (sval != null)
						write.invoke(object, Double.parseDouble(sval));
				} else if (propertyType == Enum.class) {
					if (sval != null)
						write.invoke(object, Enum.valueOf(propertyType, sval));
				} else if (
						(propertyType == float.class) ||
						(propertyType == Float.class)) {
					if (sval != null)
						write.invoke(object, Float.parseFloat(sval));
				} else if (
						(propertyType == int.class) ||
						(propertyType == Integer.class)) {
					if (sval != null)
						write.invoke(object, Integer.parseInt(sval));
				} else if (
						(propertyType == long.class) ||
						(propertyType == Long.class)) {
					if (sval != null)
						write.invoke(object, Long.parseLong(sval));
				} else if (
						(propertyType == short.class) ||
						(propertyType == Short.class)) {
					if (sval != null)
						write.invoke(object, Short.parseShort(sval));
				} else if (propertyType == String.class) {
					if (sval != null)
						write.invoke(object, sval);
				} else if (Serializable.class.isAssignableFrom(propertyType)) {
					String childObjectPrefix = prefix + pd.getName() + "."; 
					if (hasPropertiesStartingWith(properties, childObjectPrefix)) {
						Serializable childObject = (Serializable) propertyType.getConstructor((Class[]) null).newInstance((Object[]) null);
						propertiesToObject(properties, childObjectPrefix, childObject);
						write.invoke(object, childObject);
					}
				}
			}
		}
		
		for (PropertyDescriptor pd : pds) {
			if (pd instanceof IndexedPropertyDescriptor) {
				Method read = pd.getReadMethod();
				if (read == null)
					continue;

				IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
				if ((ipd.getWriteMethod() == null) && (ipd.getIndexedWriteMethod() == null))
					continue;
				
				Class indexedPropertyType = ipd.getIndexedPropertyType();
				Object items[] = propertiesToObjectArray(properties, prefix + pd.getName() + ".", indexedPropertyType);
				if (items == null)
					continue;
				
				if (ipd.getWriteMethod() != null) {
					Method write = ipd.getWriteMethod();
					write.invoke(object, new Object[] { items });
				} else {
					Method write = ipd.getIndexedWriteMethod();
					for (int i = 0; i < items.length; i++) {
						write.invoke(object, new Object[] { i, items[i] });
					}
				}
			}
		}
		return object;
	}
	
	public static <BeanObject extends Serializable> void objectToProperties(Properties properties, String prefix, BeanObject object) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (object == null)
			return;
		Class objectClass = object.getClass();
		BeanInfo beanInfo = Introspector.getBeanInfo(objectClass);
		PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			if (pd instanceof IndexedPropertyDescriptor) {
				IndexedPropertyDescriptor indexedProperty = (IndexedPropertyDescriptor) pd;
				Method read = indexedProperty.getReadMethod();
				if (read == null)
					continue;
				if ((indexedProperty.getWriteMethod() == null) && (indexedProperty.getIndexedWriteMethod() == null))
					continue;
				Object[] indexedObjects = (Object[]) read.invoke(object, (Object[]) null);
				if (indexedObjects == null)
					continue;
				properties.setProperty(prefix + indexedProperty.getName() + ".size", Integer.toString(indexedObjects.length));
				for (int i = 0; i < indexedObjects.length; i++) {
					Object indexedObject = indexedObjects[i];
					if ((indexedObject == null) || 
						(!Serializable.class.isAssignableFrom(indexedObject.getClass())))
							continue;
					objectToProperties(properties, 
							prefix + indexedProperty.getName() + "." + Integer.toString(i) + "." , 
							(Serializable) indexedObject);
				}
				continue;
			} else {
				if (pd.getWriteMethod() == null)
					continue;
				Class propertyType = pd.getPropertyType();
				Method read = pd.getReadMethod();
				Object value = read.invoke(object, (Object[]) null);
				if (value == null)
					continue;
				if (propertyType.getComponentType() != null) {
					// array
					Class arrayType = propertyType.getComponentType();
					if (Serializable.class.isAssignableFrom(arrayType)) {
						Object objects[] = (Object[]) value;
						properties.setProperty(prefix + pd.getName() + ".size", Integer.toString(objects.length));
						for (int i = 0; i < objects.length; i++) {
							String itemPrefix = prefix + pd.getName() + "." + Integer.toString(i) + "."; 
							objectToProperties(properties,
									itemPrefix, 
									(Serializable) objects[i]);
						}
					}
				} else if (
					(propertyType == boolean.class) ||
					(propertyType == Boolean.class) ||
					(propertyType == byte.class) ||
					(propertyType == Byte.class) ||
					(propertyType == Character.class) ||
					(propertyType == double.class) ||
					(propertyType == Double.class) ||
					(propertyType == Enum.class) ||
					(propertyType == float.class) ||
					(propertyType == Float.class) ||
					(propertyType == int.class) ||
					(propertyType == Integer.class) ||
					(propertyType == long.class) ||
					(propertyType == Long.class) ||
					(propertyType == short.class) ||
					(propertyType == Short.class) ||
					(propertyType == String.class)) {
					properties.setProperty(prefix + pd.getName(), value.toString());
				} else if (Serializable.class.isAssignableFrom(propertyType)) {
					objectToProperties(properties,
							prefix + pd.getName() + ".", 
							(Serializable) value);
				}
			}
		}
	}
}
