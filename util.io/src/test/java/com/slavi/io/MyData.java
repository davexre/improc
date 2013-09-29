package com.slavi.io;

import java.io.Serializable;

public final class MyData implements Serializable {

	private int id;

	private String name;

	private String comment;

	public MyData () {
	}
	
	public MyData(int id, String name) {
		setId(id);
		setName(name);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
