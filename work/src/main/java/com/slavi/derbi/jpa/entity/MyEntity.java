package com.slavi.derbi.jpa.entity;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "my_entity")
@Access(AccessType.FIELD)
@javax.persistence.DiscriminatorColumn(name = "SYS_TYPE")
public class MyEntity implements Serializable {
	@Id
	@Column(name = "sys_oid")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;

	@Column
	String data;

	@Column
	String data1;

	@Column
	String data2;

	public MyEntity() {}

	public MyEntity(int id, String data1) {
		this.id = id;
		this.data1 = data1;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getData1() {
		return data1;
	}

	public void setData1(String data1) {
		this.data1 = data1;
	}

	public String getData2() {
		return data2;
	}

	public void setData2(String data2) {
		this.data2 = data2;
	}

	@Override
	public String toString() {
		return "MyEntity [id=" + id + ", data=" + data + ", data1=" + data1 + ", data2=" + data2 + "]";
	}
}
