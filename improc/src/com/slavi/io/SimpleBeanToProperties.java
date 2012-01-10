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
				if ((propertyType == boolean.class) ||
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
		
		for (PropertyDescriptor property : pds) {
			if (property instanceof IndexedPropertyDescriptor) {
				IndexedPropertyDescriptor indexedProperty = (IndexedPropertyDescriptor) property;
				Class indexedPropertyType = indexedProperty.getIndexedPropertyType();
				if (!Serializable.class.isAssignableFrom(indexedPropertyType)) 
					continue;
				Method read = indexedProperty.getReadMethod();
				if (read == null)
					continue;
				if ((indexedProperty.getWriteMethod() == null) && (indexedProperty.getIndexedWriteMethod() == null))
					continue;
				
				String indexedPropertySizeStr = properties.getProperty(prefix + indexedProperty.getName() + ".size");
				if (indexedPropertySizeStr == null)
					continue;
				int indexedPropertySize = Integer.parseInt(indexedPropertySizeStr);
				if (indexedPropertySize < 0)
					continue;
				
				String itemsPrefix = prefix + indexedProperty.getName() + ".";
				Object items[] = (Object[]) Array.newInstance(indexedPropertyType, indexedPropertySize);
				for (int i = 0; i < indexedPropertySize; i++) {
					String itemPrefix = itemsPrefix + Integer.toString(i) + ".";
					if (hasPropertiesStartingWith(properties, itemPrefix)) {
						items[i] = (Serializable) indexedPropertyType.getConstructor((Class[]) null).newInstance((Object[]) null);
						propertiesToObject(properties, itemPrefix, (Serializable) items[i]);
					}
				}
				
				if (indexedProperty.getWriteMethod() != null) {
					Method write = indexedProperty.getWriteMethod();
					write.invoke(object, new Object[] { items });
				} else {
					Method write = indexedProperty.getIndexedWriteMethod();
					for (int i = 0; i < items.length; i++) {
						write.invoke(object, new Object[] { i, items[i] });
					}
				}
			}
		}
		return object;
	}
	
	public static <BeanObject extends Serializable> void objectToProperties(Properties properties, String prefix, BeanObject object) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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
				if (
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
