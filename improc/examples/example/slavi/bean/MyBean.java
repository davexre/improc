package example.slavi.bean;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Arrays;

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
	private int intProperty;

	// Simple string property
	private String stringProperty;

	// Simple boolean Property
	private boolean boolProperty;
	
	// Enumeration property
	private MyEnum myEnum;

	// int array, but not an Indexed Property
	private int intArrayProperty[];

	// int Indexed Property
	private int intIndexProperty[];
	
	// Complex object property
	private MyData myData;

	// Complex object array, but not an Indexed Property
	private MyData myDataArray[] = new MyData[4];

	// Complex object Indexed Property
	private MyData myDataIndexProperty[];

	// Complex object Indexed Property
	private MyData myDataIndexPropertyNoArrayWrite[] = new MyData[0];

	public int getIntProperty() {
		return intProperty;
	}

	public void setIntProperty(int intProperty) {
		this.intProperty = intProperty;
	}

	public String getStringProperty() {
		return stringProperty;
	}

	public void setStringProperty(String stringProperty) {
		this.stringProperty = stringProperty;
	}

	public boolean isBoolProperty() {
		return boolProperty;
	}

	public void setBoolProperty(boolean boolProperty) {
		this.boolProperty = boolProperty;
	}

	public MyEnum getMyEnum() {
		return myEnum;
	}

	public void setMyEnum(MyEnum myEnum) {
		this.myEnum = myEnum;
	}

	public int[] getIntArrayProperty() {
		return intArrayProperty;
	}

	public void setIntArrayProperty(int[] intArrayProperty) {
		this.intArrayProperty = intArrayProperty;
	}

	public int[] getIntIndexProperty() {
		return intIndexProperty;
	}

	public void setIntIndexProperty(int[] intIndexProperty) {
		this.intIndexProperty = intIndexProperty;
	}

	public int getIndexProperty(int index) {
		return intIndexProperty[index];
	}

	public void setIndexProperty(int index, int value) {
		intIndexProperty[index] = value;
	}

	public MyData getMyData() {
		return myData;
	}

	public void setMyData(MyData myData) {
		this.myData = myData;
	}

	public MyData[] getMyDataArray() {
		return myDataArray;
	}

	public void setMyDataArray(MyData[] myDataArray) {
		this.myDataArray = myDataArray;
	}

	public MyData[] getMyDataIndexProperty() {
		return myDataIndexProperty;
	}

	public void setMyDataIndexProperty(MyData[] myDataArray) {
		this.myDataIndexProperty = myDataArray;
	}

	public MyData getMyDataIndexProperty(int index) {
		return myDataIndexProperty[index];
	}

	public void setMyDataIndexProperty(int index, MyData myData) {
		this.myDataIndexProperty[index] = myData;
	}
	
	public int getMyDataIndexPropertyNoArrayWriteSize() {
		return myDataIndexPropertyNoArrayWrite.length;
	}
	
	public void setMyDataIndexPropertyNoArrayWriteSize(int size) {
		if (size < 0)
			return;
		myDataIndexPropertyNoArrayWrite = Arrays.copyOf(myDataIndexPropertyNoArrayWrite, size);
	}
	
	public MyData[] getMyDataIndexPropertyNoArrayWrite() {
		return myDataIndexPropertyNoArrayWrite;
	}

	public MyData getMyDataIndexPropertyNoArrayWrite(int index) {
		return myDataIndexPropertyNoArrayWrite[index];
	}

	public void setMyDataIndexPropertyNoArrayWrite(int index, MyData myData) {
		this.myDataIndexPropertyNoArrayWrite[index] = myData;
	}

	public static void main(String[] args) throws Exception {
		BeanInfo beanInfo = Introspector.getBeanInfo(MyBean.class);
		PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			if (pd instanceof IndexedPropertyDescriptor) {
				IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
				System.out.println(pd.getName());
				System.out.println("  read:        " + ipd.getReadMethod());
				System.out.println("  index read:  " + ipd.getIndexedReadMethod());
				System.out.println("  write:       " + ipd.getWriteMethod());
				System.out.println("  index write: " + ipd.getIndexedWriteMethod());
			}
		}
	}
}
