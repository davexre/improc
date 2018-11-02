package com.slavi.derbi.jpa.entity;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Embeddable
@Access(AccessType.FIELD)
public class MyNestedData implements Serializable {

	@Column
	String data1;

	public String getData1() {
		return data1;
	}

	public void setData1(String data1) {
		this.data1 = data1;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
