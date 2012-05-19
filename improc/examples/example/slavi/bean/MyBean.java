package example.slavi.bean;

import java.io.Serializable;

public class MyBean implements Serializable {
/*
	PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener p) {
		propertyChangeSupport.removePropertyChangeListener(p);
	}
*/

	// Simple int property
	int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	// Simple string property
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// Simple boolean Property
	boolean boolType;
	
	public boolean isBoolType() {
		return boolType;
	}

	public void setBoolType(boolean boolType) {
		this.boolType = boolType;
	}

	// Enumeration property
	MyEnum myEnum;

	public MyEnum getMyEnum() {
		return myEnum;
	}

	public void setMyEnum(MyEnum myEnum) {
		this.myEnum = myEnum;
	}

	// int array, but not an Indexed Property
	int arrayProperty[];

	public int[] getArrayProperty() {
		return arrayProperty;
	}

	public void setArrayProperty(int[] arrayProperty) {
		this.arrayProperty = arrayProperty;
	}

	// int Indexed Property
	int indexProperty[];
	
	public int[] getIndexProperty() {
		return indexProperty;
	}

	public void setIndexProperty(int[] indexProperty) {
		this.indexProperty = indexProperty;
	}

	public int getIndexProperty(int index) {
		return indexProperty[index];
	}

	public void setIndexProperty(int index, int value) {
		indexProperty[index] = value;
	}

	// Complex object property
	MyData myData;

	public MyData getMyData() {
		return myData;
	}

	public void setMyData(MyData myData) {
		this.myData = myData;
	}

	// Complex object array, but not an Indexed Property
	MyData objectArray[] = new MyData[4];

	public MyData[] getObjectArray() {
		return objectArray;
	}

	public void setObjectArray(MyData[] objectArray) {
		this.objectArray = objectArray;
	}

	// Complex object Indexed Property
	MyData objectIndexProperty[];

	public MyData[] getObjectIndexProperty() {
		return objectIndexProperty;
	}

	public void setObjectIndexProperty(MyData[] objectIndexProperty) {
		this.objectIndexProperty = objectIndexProperty;
	}

	public MyData getObjectIndexProperty(int index) {
		return objectIndexProperty[index];
	}

	public void setObjectIndexProperty(MyData object, int index) {
		this.objectIndexProperty[index] = object;
	}
}
