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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jdom.Element;

public class SimpleBeanToXML {
	private static class ArrayObjectData {
		int index;
		Serializable data;
	}
	
	private static Object[] xmlToObjectArray(Element root, Class arrayType) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException {
		if (!Serializable.class.isAssignableFrom(arrayType))
			return null;
		String arraySizeStr = root.getAttributeValue("size");
		if (arraySizeStr == null)
			return null;
		int arraySize = Integer.parseInt(arraySizeStr);
		if (arraySize < 0)
			return null;
		
		Object items[] = (Object[]) Array.newInstance(arrayType, arraySize);
		for (Object i : root.getChildren("item")) {
			Element e = (Element) i;
			int index = Integer.parseInt(e.getAttributeValue("index"));
			if (e.getChildren().size() == 0) {
				items[index] = null;
			} else {
				items[index] = (Serializable) arrayType.getConstructor((Class[]) null).newInstance((Object[]) null);
				xmlToObject(e, (Serializable) items[index]);
			}
		}
		return items;
	}
	
	public static <BeanObject extends Serializable> BeanObject xmlToObject(Element root, BeanObject object) throws IntrospectionException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class objectClass = object.getClass();
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
			
			if (propertyType.getComponentType() != null) {
				// array
				Class arrayType = propertyType.getComponentType();
				if (Serializable.class.isAssignableFrom(arrayType)) {
					Element theArray = root.getChild(pd.getName());
					if (theArray != null) {
						Object items[] = xmlToObjectArray(theArray, arrayType);
						if (items != null)
							write.invoke(object, new Object[] { items });
					}
				}
				continue;
			}
			
			Element svalEl = root.getChild(pd.getName());
			if (svalEl == null)
				continue;
			String sval = svalEl.getTextTrim(); 

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
			} else if ((propertyType.isEnum()) || (propertyType == Enum.class)) {
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
				Element childObjectRoot = root.getChild(pd.getName());
				if (childObjectRoot != null) {
					Serializable childObject = (Serializable) propertyType.getConstructor((Class[]) null).newInstance((Object[]) null);
					xmlToObject(childObjectRoot, childObject);
					write.invoke(object, childObject);
				}
			}
		}
		
		for (PropertyDescriptor pd : pds) {
			if (pd instanceof IndexedPropertyDescriptor) {
				IndexedPropertyDescriptor indexedProperty = (IndexedPropertyDescriptor) pd;
				Class indexedPropertyType = indexedProperty.getIndexedPropertyType();
				if (!Serializable.class.isAssignableFrom(indexedPropertyType)) 
					continue;
				Method read = indexedProperty.getReadMethod();
				if (read == null)
					continue;
				if ((indexedProperty.getWriteMethod() == null) && (indexedProperty.getIndexedWriteMethod() == null))
					continue;
				
				Element theArray = root.getChild(pd.getName());
				if (theArray == null)
					continue;
				
				List itemElements = theArray.getChildren("item");
				ArrayList<ArrayObjectData> items = new ArrayList<ArrayObjectData>();
				boolean hasItemsWithoutIndex = false;
				int maxIndex = -1;
				for (int i = 0; i < itemElements.size(); i++) {
					Element item = (Element) itemElements.get(i);
					
					ArrayObjectData itemData = new ArrayObjectData();
					try {
						itemData.index = Integer.parseInt(item.getAttributeValue("index"));
						if (maxIndex < itemData.index)
							maxIndex = itemData.index;
					} catch (Exception e) {
						itemData.index = -1;
						hasItemsWithoutIndex = true;
					}
					itemData.data = (Serializable) indexedPropertyType.getConstructor((Class[]) null).newInstance((Object[]) null);
					xmlToObject(item, itemData.data);
					items.add(itemData);
				}
				
				if (hasItemsWithoutIndex) {
					Collections.sort(items, new Comparator<ArrayObjectData>() {
						public int compare(ArrayObjectData o1, ArrayObjectData o2) {
							if ((o1.index >= 0) && (o2.index >= 0)) {
								return (o1.index < o2.index ? -1 : (o1.index == o2.index ? 0 : 1));
							}
							if ((o1.index >= 0) && (o2.index < 0)) {
								return 1;
							}
							if ((o1.index < 0) && (o2.index >= 0)) {
								return -1;
							}
							return 0;
						}
					});
					for (int i = 0; i < items.size(); i++)
						items.get(i).index = i;
					maxIndex = items.size() - 1;
				}
				
				if (indexedProperty.getWriteMethod() != null) {
					Object data[] = (Object[]) Array.newInstance(indexedPropertyType, maxIndex + 1); 
					for (ArrayObjectData item : items) {
						data[item.index] = item.data;
					}
					Method write = indexedProperty.getWriteMethod();
					write.invoke(object, new Object[] { data });
				} else {
					Method write = indexedProperty.getIndexedWriteMethod();
					for (ArrayObjectData item : items) {
						write.invoke(object, new Object[] { item.index, item.data });
					}
				}
			}
		}
		return object;
	}
	
	public static <BeanObject extends Serializable> void objectToXml(Element root, BeanObject object) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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
				Element theArray = new Element(pd.getName());
				for (int i = 0; i < indexedObjects.length; i++) {
					Object indexedObject = indexedObjects[i];
					if ((indexedObject == null) || 
						(!Serializable.class.isAssignableFrom(indexedObject.getClass())))
							continue;
					Element item = new Element("item");
					item.setAttribute("index", Integer.toString(i));
					objectToXml(item, (Serializable) indexedObject);
					theArray.addContent(item);
				}
				root.addContent(theArray);
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
						Element theArray = new Element(pd.getName());
						theArray.setAttribute("size", Integer.toString(objects.length));
						for (int i = 0; i < objects.length; i++) {
							Element item = new Element("item");
							item.setAttribute("index", Integer.toString(i));
							objectToXml(item, (Serializable) objects[i]);
							theArray.addContent(item);
						}
						root.addContent(theArray);
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
					(propertyType.isEnum()) ||
					(propertyType == float.class) ||
					(propertyType == Float.class) ||
					(propertyType == int.class) ||
					(propertyType == Integer.class) ||
					(propertyType == long.class) ||
					(propertyType == Long.class) ||
					(propertyType == short.class) ||
					(propertyType == Short.class) ||
					(propertyType == String.class)) {
					Element el = new Element(pd.getName());
					el.setText(value.toString());
					root.addContent(el);
				} else if (Serializable.class.isAssignableFrom(propertyType)) {
					Element obj = new Element(pd.getName());
					objectToXml(obj, (Serializable) value);
					root.addContent(obj);
				}
			}
		}
	}
}
