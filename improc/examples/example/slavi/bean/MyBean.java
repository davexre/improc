package example.slavi.bean;

import java.io.Serializable;

public class MyBean implements Serializable {

	String name;

	int id;
	
//	MyData data0 = new MyData();

	MyData data[] = new MyData[0];
/*
	PropertyChangeSupport propertyChangeSupport;

	public MyBean() {
		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener p) {
		propertyChangeSupport.removePropertyChangeListener(p);
	}
*/
	
	public MyBean() {
		
	}
	
	public MyBean(int id, String name, MyData myData) {
		setId(id);
		setName(name);
//		setData0(myData);
		setData(new MyData[] { null, null, myData });
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	public MyData[] getData() {
		return data;
	}

	public void setData(MyData data[]) {
		this.data = data;
	}
/*
	public MyData getData(int i) {
		return data[i];
	}

	public void setData(int i, MyData d) {
		data[i] = d;
	}
*/
/*
	public MyData[] getIndexedData() {
		return data;
	}

	public MyData getIndexedData(int i) {
		return data[i];
	}

	public void setIndexedData(int i, MyData d) {
		data[i] = d;
	}
*/
/*
	public MyData getData0() {
		return data0;
	}

	public void setData0(MyData data0) {
		this.data0 = data0;
	}
*/
}
