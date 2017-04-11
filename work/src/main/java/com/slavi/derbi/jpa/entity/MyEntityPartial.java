package com.slavi.derbi.jpa.entity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
@Table(name = "my_entity")
@Access(AccessType.FIELD)
public class MyEntityPartial {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;

	@Column
	String data;

	@Embedded
	MyNestedData nestedData;

	public MyNestedData getNestedData() {
		return nestedData;
	}

	public void setNestedData(MyNestedData nestedData) {
		this.nestedData = nestedData;
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

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
